/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services

import config.{PbikAppConfig, PbikSessionCache}
import controllers.FakePBIKApplication
import models.{AuthenticatedRequest, Bik, EiLPerson, EmpRef, PbikSession, RegistrationItem, RegistrationList}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import scala.concurrent.duration.{DurationInt, FiniteDuration}

class SessionServiceSpec extends UnitSpec with FakePBIKApplication with BeforeAndAfterEach with MockitoSugar {

  override lazy val fakeApplication: Application = GuiceApplicationBuilder(
    disabled = Seq(classOf[com.kenshoo.play.metrics.PlayModule])
  ).configure(config)
    .overrides(bind[SessionService].toInstance(mockSessionService))
    .build()

  val timeout: FiniteDuration = 5 seconds
  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("session001")))
  val TestSessionService: SessionService = new SessionService(mockHttp, mockPbikSessionCache)

  val pbikSession: PbikSession = PbikSession(None, None, None, None, None, None, None)

  "The SessionService" should {

    "cache a list of registrations" in {
      val regList = RegistrationList(None, List(RegistrationItem("31", true, true)), None)
      when(mockPbikSessionCache.fetchAndGetEntry[PbikSession](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))
      val json = Json.toJson[PbikSession](pbikSession.copy(registrations = Some(regList)))
      when(mockPbikSessionCache.cache[PbikSession](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(CacheMap("sessionValue", Map("pbik_session" -> json))))
      val result = await(TestSessionService.cacheRegistrationList(regList))(timeout)
      result shouldBe Some(pbikSession.copy(registrations = Some(regList)))
      println(result)
    }

    "cache a bik to remove" in {
      val bikRemoved = RegistrationItem("31", true, true)
      when(mockPbikSessionCache.fetchAndGetEntry[PbikSession](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))
      val json = Json.toJson[PbikSession](pbikSession.copy(bikRemoved = Some(bikRemoved)))
      when(mockPbikSessionCache.cache[PbikSession](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(CacheMap("sessionValue", Map("pbik_session" -> json))))
      val result = await(TestSessionService.cacheBikRemoved(bikRemoved))(timeout)
      result shouldBe Some(pbikSession.copy(bikRemoved = Some(bikRemoved)))
    }

    "cache a list of matches" in {
      val listOfMatches = List(EiLPerson("AA111111A", "John", None, "Smith", None, None, None, None))
      when(mockPbikSessionCache.fetchAndGetEntry[PbikSession](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))
      val json = Json.toJson[PbikSession](pbikSession.copy(listOfMatches = Some(listOfMatches)))
      when(mockPbikSessionCache.cache[PbikSession](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(CacheMap("sessionValue", Map("pbik_session" -> json))))
      val result = await(TestSessionService.cacheListOfMatches(listOfMatches))(timeout)
      result shouldBe Some(pbikSession.copy(listOfMatches = Some(listOfMatches)))
    }

    "cache an EiLPerson" in {
      val eiLPerson = EiLPerson("AA111111A", "John", None, "Smith", None, None, None, None)
      when(mockPbikSessionCache.fetchAndGetEntry[PbikSession](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))
      val json = Json.toJson[PbikSession](pbikSession.copy(eiLPerson = Some(eiLPerson)))
      when(mockPbikSessionCache.cache[PbikSession](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(CacheMap("sessionValue", Map("pbik_session" -> json))))
      val result = await(TestSessionService.cacheEiLPerson(eiLPerson))(timeout)
      result shouldBe Some(pbikSession.copy(eiLPerson = Some(eiLPerson)))
    }

    "cache the current exclusions" in {
      val currentExclusions = List(EiLPerson("AA111111A", "John", None, "Smith", None, None, None, None))
      when(mockPbikSessionCache.fetchAndGetEntry[PbikSession](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))
      val json = Json.toJson[PbikSession](pbikSession.copy(currentExclusions = Some(currentExclusions)))
      when(mockPbikSessionCache.cache[PbikSession](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(CacheMap("sessionValue", Map("pbik_session" -> json))))
      val result = await(TestSessionService.cacheCurrentExclusions(currentExclusions))(timeout)
      result shouldBe Some(pbikSession.copy(currentExclusions = Some(currentExclusions)))
    }

    "cache the current year registered biks" in {
      val cyRegisteredBiks = List(Bik("31", 30))
      when(mockPbikSessionCache.fetchAndGetEntry[PbikSession](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))
      val json = Json.toJson[PbikSession](pbikSession.copy(cyRegisteredBiks = Some(cyRegisteredBiks)))
      when(mockPbikSessionCache.cache[PbikSession](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(CacheMap("sessionValue", Map("pbik_session" -> json))))
      val result = await(TestSessionService.cacheCYRegisteredBiks(cyRegisteredBiks))(timeout)
      result shouldBe Some(pbikSession.copy(cyRegisteredBiks = Some(cyRegisteredBiks)))
    }

    "cache the next year registered biks" in {
      val nyRegisteredBiks = List(Bik("31", 30))
      when(mockPbikSessionCache.fetchAndGetEntry[PbikSession](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))
      val json = Json.toJson[PbikSession](pbikSession.copy(nyRegisteredBiks = Some(nyRegisteredBiks)))
      when(mockPbikSessionCache.cache[PbikSession](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(CacheMap("sessionValue", Map("pbik_session" -> json))))
      val result = await(TestSessionService.cacheNYRegisteredBiks(nyRegisteredBiks))(timeout)
      result shouldBe Some(pbikSession.copy(nyRegisteredBiks = Some(nyRegisteredBiks)))
    }

    "be able to fetch the pbik session" in {
      val pbikSession = PbikSession(None, Some(RegistrationItem("31", true, true)), None, None, None, None, None)
      when(mockPbikSessionCache.fetchAndGetEntry[PbikSession](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(pbikSession)))
      val result = await(TestSessionService.fetchPbikSession())(timeout)
      result shouldBe Some(pbikSession)
    }

    "return a clean session if no session is found" in {
      when(mockPbikSessionCache.fetchAndGetEntry[PbikSession](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))
      val result = await(TestSessionService.fetchPbikSession())(timeout)
      result shouldBe Some(cleanSession)
    }

  }

}
