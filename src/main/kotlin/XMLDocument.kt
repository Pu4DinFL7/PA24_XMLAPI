data class XMLDocument(
    private val rootEntity: XMLEntity,
    private val specifications: String = ""

){

    fun printAllEntities(entity:XMLEntity){
        for (child in entity.childrens){
            println(child)
            printAllEntities(child)
        }
    }
    fun getRootEntity(): XMLEntity {
        return rootEntity
    }
    private fun getSpecifications(): String {
        return specifications
    }

    fun addEntity(entityName: String, entityParent: String):XMLEntity? {
        val rootEntity = getRootEntity()
        return addEntityRecursively(rootEntity, entityName, entityParent)
    }
    private fun addEntityRecursively(entity: XMLEntity, entityName: String, entityParent: String): XMLEntity? {
        // se o parent for a propria root faz o if seguinte:
        if(entityParent == entity.name) {
            val parent = XMLEntity(entityParent, entity.parent)
            return XMLEntity(entityName, parent)
        }
        // senao vai iterando pelos vários filhos ate encontrar o parent correspondente ao passado nos argumentos
        for (child in entity.childrens) {
            if (child.name == entityParent) {
                val parent = XMLEntity(entityParent, child.parent)
                return XMLEntity(entityName, parent)
            } else {
                val result = addEntityRecursively(child, entityName, entityParent)
                if (result != null) {
                    return result
                }
            }
        }
        return null
    }



    fun removeEntity(entityName: String){
        val rootEntity = getRootEntity()
        return removeEntityRecursively(rootEntity,entityName)
    }
    private fun removeEntityRecursively(entity: XMLEntity, entityName: String) {
        val iterator = entity.childrens.iterator()
        while (iterator.hasNext()) {
            val child = iterator.next()
            if (child.name == entityName) {
                iterator.remove() // Remove the child safely using the iterator
            } else {
                removeEntityRecursively(child, entityName)

            }
        }
    }

}

fun main(){
    val a = XMLEntity("a")
    val b = XMLEntity("b", a)
    val c = XMLEntity("c", a)
    val d = XMLEntity("d", b)
    val doc = XMLDocument(a)
    doc.addEntity("b",a.name)
    doc.addEntity("c",a.name)
    doc.addEntity("d",b.name)
    doc.addEntity("e",d.name)
    doc.printAllEntities(a)
    //println(doc.getRootEntity().childrens)
    println("_______________________________________________________________________")
    doc.removeEntity("e")
    doc.printAllEntities(a)
    //println(doc.getRootEntity().childrens.forEach{c -> println(c)})

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

