package com.ayai.main.networking

import akka.actor._
import akka.routing.RoundRobinRouter
import akka.util.Duration
import akka.util.duration._

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props

abstract class Connection extends Actor {
  def read(): String

  def write(json: String)

  def receive
}
