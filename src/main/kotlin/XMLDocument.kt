class XMLDocument{
    private val entities = mutableListOf<XMLEntity>()
    private val specifications: String = ""


    fun addEntity(entity:XMLEntity){

    }

    fun removeEntity(entity:XMLEntity){

    }

}

data class XMLEntity(
     val name: String,
     val parent: XMLEntity? = null,
) {
    val childrens = mutableListOf<XMLEntity>()
    val atributes: String? = null
    val plainText:String? = null
}

