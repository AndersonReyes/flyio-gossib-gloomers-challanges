package com.github.andersonreyes.api

object Counter {
  private[this] var c: Long = 0

  def get: Long = c
  def increment: Long = synchronized {
    c += 1
    c
  }
}
