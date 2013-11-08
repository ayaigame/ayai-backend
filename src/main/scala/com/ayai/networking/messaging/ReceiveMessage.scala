package com.ayai.main.networking.messaging

case class ReceiveMessage(m: String, s: String) extends Message {
  val message = m
  val sender = s
}
