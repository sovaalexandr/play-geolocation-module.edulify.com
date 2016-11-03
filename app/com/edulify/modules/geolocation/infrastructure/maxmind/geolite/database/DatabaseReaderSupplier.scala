package com.edulify.modules.geolocation.infrastructure.maxmind.geolite.database

import java.io._
import java.net.URL
import java.nio.file.Files
import java.util.zip.GZIPInputStream
import javax.inject.Inject

import akka.Done
import akka.actor.{Actor, Cancellable}
import akka.dispatch.Futures
import akka.stream.Materializer
import com.maxmind.db.CHMCache
import com.maxmind.geoip2.DatabaseReader
import com.maxmind.geoip2.exception.HttpException
import play.api.Configuration
import play.api.cache.CacheApi
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.libs.ws.{StreamedResponse, WSClient, WSResponseHeaders}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object DatabaseReaderSupplier {
  case object GetCurrent
  case object RenewDB
}

class DatabaseReaderSupplier @Inject() (
                                         configuration: Configuration,
                                         ws: WSClient,
                                         cache: CacheApi,
                                         durationProvider: DurationToUpdateProvider
                                         ) (implicit val mat: Materializer) extends Actor {

  implicit val ec = context.dispatcher

  private val baseUrl = configuration
    .getString("geolocation.geolite.dbUrl")
    .getOrElse("http://geolite.maxmind.com/download/geoip/database/GeoLite2-City.mmdb.gz")

  private val gZipped = configuration
    .getBoolean("geolocation.geolite.gzipped")
    .getOrElse(true)

  //DB file changes are not required on reload because whole DB is handled in-memory by nio under hood of DatabaseReader
  private val dbFile = new File(
    configuration
      .getString("geolocation.geolite.dbFile")
      .getOrElse(System.getProperty("java.io.tmpdir") + "/GeoLite2-City.mmdb")
  )

  private var updates: Cancellable = null
  private var reader: DatabaseReader = null

  @throws[Exception](classOf[Exception])
  override def preStart() = {
    if (dbFile.isFile) {
      createReader()
      sceduleNextUpdate()
    } else {
      renewDataBase().onComplete { _ => {
          createReader()
          sceduleNextUpdate()
        }
      }
    }
    super.preStart()
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    closeReader()
    updates cancel()
    super.postStop()
  }

  override def receive = {

    case DatabaseReaderSupplier.GetCurrent => sender ! reader

    case DatabaseReaderSupplier.RenewDB => renewDataBase()
      .onComplete { _ =>
        closeReader()
        createReader()
        sceduleNextUpdate()
    }

    case other => unhandled(other)
  }

  private def createReader(): Unit = {
    reader = new DatabaseReader
      .Builder(dbFile)
      .withCache(new CHMCache()) // Each reader have to get it's own cache because DB could be already changed
      .build()
  }

  /**
   * DB reader should be closed correctly
   */
  private def closeReader(): Unit = {
    reader close()
  }

  private def renewDataBase() = {
    val url = baseUrl
    var request = ws.url(url).withMethod("GET")

    val eTagKey = url + HeaderNames.ETAG
    request = cache.get(eTagKey).map(eTag => request.withHeaders((HeaderNames.IF_NONE_MATCH,     eTag))).getOrElse(request)
    val dateKey = url + HeaderNames.LAST_MODIFIED
    request = cache.get(dateKey).map(date => request.withHeaders((HeaderNames.IF_MODIFIED_SINCE, date))).getOrElse(request)

    request.stream()
      .flatMap {
        case StreamedResponse(response, body) =>
          response.status match {
            case Status.OK =>
              response.headers.get(HeaderNames.CONTENT_TYPE).flatMap(_.headOption).get match {
                case MimeTypes.BINARY =>
                  val destinationFileName = if (gZipped) dbFile.getPath+".gz" else dbFile.getPath
                  val destinationFile = new File(destinationFileName)
                  val destinationStream = new FileOutputStream(destinationFile)

                  var download = body.runForeach({ bytes => destinationStream.write(bytes.toArray) })
                  if (gZipped) {
                    download = download
                      .andThen({ case result =>
                        Files.copy(new GZIPInputStream(new FileInputStream(destinationFile)), dbFile.toPath)
                        destinationStream close()
                      })
                  }
                  val cacheHeader = getHeaderCacher(download, response)
                  cacheHeader(eTagKey, HeaderNames.ETAG)
                  cacheHeader(dateKey, HeaderNames.LAST_MODIFIED)
                  download map { _ => dbFile }

                case other => throw new HttpException(s"Unexpected content type got: $other", Status.OK, new URL(url))
              }
            // Got nothing to do - last version of a file is already at system
            case Status.NOT_MODIFIED => Futures.successful(dbFile)
            case other => throw new HttpException(s"Unexpected status got: $other", other, new URL(url))
          }
        case _ => throw new IOException("Failed to get response")
      }
  }

  private def getHeaderCacher(download: Future[Done], response: WSResponseHeaders) = {
    {
      (cacheKey: String, headerName: String) => download onComplete {
        case Success(_) => response.headers get headerName flatMap(_.headOption) foreach { header => cache.set(cacheKey, header) }
        case Failure(e) => // Do nothing
      }
    }
  }

  private def sceduleNextUpdate(): Unit = {
    Option(updates) foreach { cancellable => cancellable cancel() }
    updates = context.system.scheduler.scheduleOnce(durationProvider.getDurationToNextUpdate, self, DatabaseReaderSupplier.RenewDB)
  }
}
