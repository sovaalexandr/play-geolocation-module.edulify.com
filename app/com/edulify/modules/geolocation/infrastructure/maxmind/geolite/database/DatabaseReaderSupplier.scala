package com.edulify.modules.geolocation.infrastructure.maxmind.geolite.database

import java.io._
import java.net.URL
import java.nio.file.Files
import java.util.zip.GZIPInputStream
import javax.inject.Inject

import akka.actor.Actor
import akka.dispatch.Futures
import akka.stream.Materializer
import com.maxmind.db.CHMCache
import com.maxmind.geoip2.DatabaseReader
import com.maxmind.geoip2.exception.HttpException
import play.api.Configuration
import play.api.cache.CacheApi
import play.api.http.{MimeTypes, HeaderNames, Status}
import play.api.libs.ws.{StreamedResponse, WSClient}

object DatabaseReaderSupplier {
  case object GetCurrent
  case object RenewDB
}

class DatabaseReaderSupplier @Inject() (configuration: Configuration, ws: WSClient, cache: CacheApi) (implicit val mat: Materializer) extends Actor {

  implicit val ec = context.dispatcher

  private val baseUrl = configuration
    .getString("geolocation.geolite.dbUrl")
    .getOrElse("http://geolite.maxmind.com/download/geoip/database/GeoLite2-City.mmdb.gz")

  //DB file changes are not required on reload because whole DB is handled in-memory by nio under hood of DatabaseReader
  private val dbFile = new File(
    configuration
      .getString("geolocation.geolite.dbFile")
      .getOrElse(System.getProperty("java.io.tmpdir") + "/GeoLite2-City.mmdb")
  )

  private var reader: DatabaseReader = null

  @throws[Exception](classOf[Exception])
  override def preStart() = {
    dbFile.isFile match {
      case true => createReader()
      case false => renewDataBase().onComplete { _ => createReader() }
    }
    super.preStart()
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    closeReader()
    super.postStop()
  }

  override def receive = {

    case DatabaseReaderSupplier.GetCurrent => sender ! reader

    case DatabaseReaderSupplier.RenewDB => renewDataBase()
      .onComplete { some =>
          closeReader()
          createReader()
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
    reader.close()
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
                  val gZippedDbFile = new File(dbFile.getPath+".gz")
                  val gZippedDbFileStream = new FileOutputStream(gZippedDbFile)

                  body.runForeach({ bytes => gZippedDbFileStream.write(bytes.toArray)})
                    .andThen({ case result =>
                      Files.copy(new GZIPInputStream(new FileInputStream(gZippedDbFile)), dbFile.toPath)
                      gZippedDbFileStream.close()
                    }).map( _ => {
                      response.headers.get(HeaderNames.ETAG).flatMap(_.headOption).get match {
                        case eTag:String => cache.set(eTagKey, eTag)
                      }
                      response.headers.get(HeaderNames.LAST_MODIFIED).flatMap(_.headOption).get match {
                        case date:String => cache.set(dateKey, date)
                      }
                      dbFile
                    })

                case other => throw new HttpException(s"Unexpected content type got: $other", Status.OK, new URL(url))
              }
            // Got nothing to do - last version of a file is already at system
            case Status.NOT_MODIFIED => Futures.successful(dbFile)
            case other => throw new HttpException(s"Unexpected status got: $other", other, new URL(url))
          }
        case _ => throw new IOException("Failed to get response")
      }
  }
}
