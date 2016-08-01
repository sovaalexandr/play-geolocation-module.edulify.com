package com.edulify.modules.geolocation.infrastructure.maxmind.geolite.database

import java.io.File

import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.FileIO
import akka.testkit.TestActorRef
import akka.util.Timeout
import com.maxmind.geoip2.DatabaseReader
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Application
import play.api.cache.CacheApi
import play.api.http.{MimeTypes, Status, HeaderNames}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{StreamedResponse, WSClient, WSRequest, WSResponseHeaders}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Success

/**
 * Functional test for core reader actor
 */
class DatabaseReaderSupplierTest extends PlaySpec with MockitoSugar with OneAppPerSuite {
  private val file = "test/resources/GeoLite2-City.mmdb"
  val archive = file + ".gz"

  implicit override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(Map("geolocation.geolite.dbFile" -> file))
    .build()

  implicit val system = app.actorSystem
  implicit val mat = ActorMaterializer()
  implicit val timeout = Timeout(30.millis)

  val mockWsClient = mock[WSClient]
  val mockCache = mock[CacheApi]

  val actorRef = TestActorRef(new DatabaseReaderSupplier(app.configuration, mockWsClient, mockCache))
  val url = "http://geolite.maxmind.com/download/geoip/database/GeoLite2-City.mmdb.gz"
  val eTagKey = url + HeaderNames.ETAG
  val lastModifiedKey = url + HeaderNames.LAST_MODIFIED

  "An Actor" must {
    "renew DatabaseReader uncached" in {
      val mockWSRequest = mock[WSRequest]
      when(mockWSRequest.withMethod("GET")) thenReturn mockWSRequest
      when(mockWsClient.url(url)) thenReturn mockWSRequest
      when(mockCache.get(eTagKey)) thenReturn None
      when(mockCache.get(lastModifiedKey)) thenReturn None

      val mockHeaders = mock[WSResponseHeaders]
      val mockBody = FileIO.fromFile(new File(archive))
      when(mockWSRequest.stream()) thenReturn Future.successful(StreamedResponse(mockHeaders, mockBody))
      when(mockHeaders.status) thenReturn Status.OK
      when(mockHeaders.headers) thenReturn Map(HeaderNames.CONTENT_TYPE -> Seq(MimeTypes.BINARY))

      actorRef ! DatabaseReaderSupplier.RenewDB
    }
    "supply DatabaseReader without cache/WS calls" in {
      val future = actorRef ? DatabaseReaderSupplier.GetCurrent
      val Success(result: DatabaseReader) = future.value.get

      result mustBe a [DatabaseReader]
    }
  }
}
