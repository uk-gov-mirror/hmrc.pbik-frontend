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

package connectors

import javax.inject.Inject
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.play.bootstrap.filters.frontend.crypto.SessionCookieCrypto
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.partials.{FormPartialRetriever, HeaderCarrierForPartialsConverter}

class FormPartialProvider @Inject()(val httpGet: HttpClient, cookieCrypto: SessionCookieCryptoFilterWrapper)
    extends FormPartialRetriever {

  override val crypto: String => String = cookieCrypto.encryptCookieString _
}

class PBIKHeaderCarrierForPartialsConverter @Inject()(cookieCrypto: SessionCookieCryptoFilterWrapper)
    extends HeaderCarrierForPartialsConverter {
  override val crypto: String => String = cookieCrypto.encryptCookieString _
}

class SessionCookieCryptoFilterWrapper @Inject()(sessionCookieCrypto: SessionCookieCrypto) {

  def encryptCookieString(cookie: String): String =
    sessionCookieCrypto.crypto.encrypt(PlainText(cookie)).value
}
