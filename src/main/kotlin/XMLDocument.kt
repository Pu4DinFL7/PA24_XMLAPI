data class XMLDocument(
    private val rootEntity: XMLEntity,
    private val specifications: String = ""

)
    fun addEntity(entity:XMLEntity){

    }

    fun removeEntity(entity:XMLEntity){

    }



data class XMLEntity(
      val name: String,
      val parent: XMLEntity? = null,
      val atributes: HashMap<String,String>? = null,
      val plainText:String? = null
) {
    init{
        parent?.childrens?.add(this)
    }
     val childrens = mutableListOf<XMLEntity>()

}

