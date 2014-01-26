package ayai.maps

class Tilesets(sets : List[String]) {
	override def toString : String = {
		var json = ""
		var first : Boolean = true
		for(set <- sets) {
			if(first) {
				json += "{ \"image\" : \"" + set + "\"} " 
				first = false
			}
			else 
				json += "{ \"image\" : \"" + set + "\"} "
		}
		json
	}
}