package com.github.andersonreyes.api

import io.circe.generic.extras._

object Config {
  implicit val config: Configuration =
    Configuration.default.withSnakeCaseMemberNames
}
