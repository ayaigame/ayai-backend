package com.ayai.main.networking

abstract class Connection(ip_ : String, port_ : Int) {
	val ip: String = ip_
	val port: Int = port_

	def run(): Unit
}
