import java.io.File
import java.io.PrintWriter
import javax.naming.directory.NoSuchAttributeException

data class XMLDocument(
    private val rootEntity: XMLEntity,
    var version:String? = "1.0",
    var encoding:String? = "UTF-8"
){

    val specifications: String
        get() {
            return "<?xml version=\"$version\" encoding=\"$encoding\"?>"
        }

    fun printAllEntities(){
        for (child in getRootEntity().childrens){
            println(child)
            printAllEntitiesRecursively(child)
        }
    }

    fun printAllEntitiesRecursively(entity: XMLEntity){
        for (child in entity.childrens){
            println(child)
            if(entity.childrens == null){
                return
            }
            printAllEntitiesRecursively(child)
        }
    }
    fun toTree(): String {
        val rootEntity = getRootEntity()
        return buildTree(rootEntity, mutableSetOf(), "")
    }

    private fun buildTree(entity: XMLEntity, visited: MutableSet<String>, prefix: String): String {
        val sb = StringBuilder()
        sb.append(prefix)
        sb.append(entity.name)
        sb.append("\n")

        // Add the entity name to the visited set
        visited.add(entity.name)

        // Recursively build the tree for each child entity
        for (child in entity.childrens) {
            // Add child entity only if it hasn't been visited before
            if (visited.add(child.name)) {
                sb.append(buildTree(child, visited, "$prefix  |")) // Add indentation for child entities
            }
        }

        return sb.toString()
    }

    fun getRootEntity(): XMLEntity {
        return rootEntity
    }

    fun addEntity(entityName: String, entityParent: String):XMLEntity? {
        val rootEntity = getRootEntity()
        return addEntityRecursively(rootEntity, entityName, entityParent)
    }
    private fun addEntityRecursively(entity: XMLEntity, entityName: String, entityParent: String): XMLEntity? {
        // se o parent for a propria root faz o if seguinte:
        if(entityParent == entity.name) {
            val parent = XMLEntity(entityParent, entity.parent)
            entity.childrens.add(XMLEntity(entityName, parent))
            return XMLEntity(entityName, parent)
        }
        // senao vai iterando pelos vários filhos ate encontrar o parent correspondente ao passado nos argumentos
        for (child in entity.childrens) {
            if (child.name == entityParent) {
                val parent = XMLEntity(entityParent, child.parent)
                child.childrens.add(XMLEntity(entityName, parent))
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


    // adicionar atributo passando apenas o nome da entidade e o atributo (key,value)
    fun addAttribute(entityName:String, key: String, value: String) {
        addAttributeRecursively(getRootEntity(), entityName, key, value)
    }

    fun addAttributeRecursively(entity:XMLEntity?, entityName: String, key: String, value: String){
        if (entity == null) return

        if(entity.name == entityName)
            entity.addAttribute(key,value)

        for (child in entity.childrens)
            addAttributeRecursively(child, entityName, key, value)
    }

    //
    private fun applyAttributeRecursively(entity: XMLEntity?, key: String, value: String, accept: (String) -> Boolean, apply: (XMLEntity, String, String) -> Unit) {
        if (entity == null) return

        if (accept(entity.name)) {
            apply(entity, key, value)
        }

        for (child in entity.childrens) {
            applyAttributeRecursively(child, key, value, accept, apply)
        }
    }

    //adiciona um atributo a todas as entidades com determinado nome através de uma função de 2a ordem em que o user escreve a condição que quer ver satisfeita para que seja adicionado esse atributo

    fun addToAllEntitiesAttribute(key: String, value: String, accept: (String) -> Boolean) {
        val rootEntity = getRootEntity()
        applyAttributeRecursively(rootEntity, key, value, accept) { entity, k, v ->
            entity.addAttribute(k, v)
        }
    }


    fun addAttributeVisitor(entityName: String, key:String, value:String){
        val attribute = XMLAttributeAdder(entityName, key,value, XMLAttributeAdder.Operation.ADD)
        getRootEntity().accept(attribute)
    }
    fun removeToAllEntitiesAttribute(key: String, value: String, accept: (String) -> Boolean) {
        val rootEntity = getRootEntity()
        applyAttributeRecursively(rootEntity, key, value, accept) { entity, k, _ ->
            entity.removeAttribute(k)
        }
    }

    fun toXML(name: String, savePath: String = "${name}.xml"){
        val xmlCollector = XMLTextCollector()
        xmlCollector.collectedText = specifications
        getRootEntity().accept(xmlCollector)
        val file = File(savePath)
        PrintWriter(file).use { out ->
            out.write(xmlCollector.collectedText)
        }

    }
    fun queryXPath(expression: String): List<String> {
        val entities = mutableListOf<String>()
        val parts = expression.split("/")

        var currentEntities = listOf(getRootEntity())

        for (part in parts) {
            val newEntities = mutableListOf<XMLEntity>()
            for (entity in currentEntities) {
                for (child in entity.childrens) {
                    if (child.name == part) {
                        newEntities.add(child)
                    }
                }
            }
            currentEntities = newEntities
        }
        currentEntities.forEach{e -> entities.add(e.xmlBegginerTag)}

        return entities
    }
}

fun main(){
    val a = XMLEntity("a")
    val b = XMLEntity("b", a)
    val b2 = XMLEntity("b", a)
    val c = XMLEntity("c", a)
    val d = XMLEntity("d", b)
    val e = XMLEntity("e", d)
    val doc = XMLDocument(a)
    //doc.addAttribute(b.name, "código", "231")
    //doc.addToAllEntitiesAttribute("Cayde", "06"){entityName-> entityName== "b"}
    doc.addAttributeVisitor("b", "Banshee", "44")
    println(b.getAttributes())
    println(b2.getAttributes())
    //println(doc.printAllEntities())
   /* doc.addEntity("b",a.name)
    doc.addEntity("c",a.name)
    doc.addEntity("d",b.name)
    doc.addEntity("e",d.name)*/
    //doc.printAllEntities()
    //println(doc.getRootEntity().childrens)
   /* println("_______________________________________________________________________")
    print(doc.addEntity("e", d.name))
    println("EPAH pleasee")
    println(doc.toTree())
    println("_______________________________________________________________________")*/
    //doc.removeEntity("b")
    //doc.printAllEntities()
    //println(doc.getRootEntity().childrens.forEach{c -> println(c)})

}

interface XMLVisitor{
    fun visit(entity: XMLEntity)
    fun endVisit(entity: XMLEntity){}
}
class XMLAttributeAdder(
    private val entityName: String,
    private val key: String,
    private val value: String?,
    private val operation: Operation
): XMLVisitor {

    enum class Operation{
        ADD,EDIT,REMOVE
    }
    override fun visit(entity: XMLEntity) {
        if (entity.name == entityName) {
            if(operation == Operation.ADD) {
                if (value != null) {
                    entity.addAttribute(key, value)
                }
            }
            if(operation == Operation.REMOVE){
                entity.removeAttribute(key)
            }

            if(operation == Operation.EDIT){
                if (value != null) {
                    entity.editAttribute(key,value)
                }
            }
        }
    }

}
class XMLTextCollector : XMLVisitor {
    var collectedText:String = ""
    private var indentationLevel: Int = 0
    override fun visit(entity: XMLEntity) {

        collectedText += "\n"+"\t".repeat(indentationLevel)
        indentationLevel++

        if(entity.entityPlainText != "")
            collectedText += entity.xmlBegginerTag + entity.entityPlainText
        else
            collectedText += entity.xmlBegginerTag
    }

    override fun endVisit(entity: XMLEntity) {
        indentationLevel--
        if(!entity.isSelfClosing)
            if(entity.entityPlainText != "") {
                collectedText += entity.xmlEndTag
            }else
                collectedText += "\n" +"\t".repeat(indentationLevel)+ entity.xmlEndTag

    }
}
data class XMLEntity(
    var name: String,
    var parent: XMLEntity? = null,
    private var attributesMap: HashMap<String,String>? = null,
    private var plainText:String? = ""
) {
    val childrens = mutableListOf<XMLEntity>()
    init{
        parent?.childrens?.add(this)
    }

    var entityPlainText: String?
        get() = plainText
        set(value) {
            plainText = value
        }
    val fullName: String
        get() {
            var aux = name
            attributesMap?.forEach{aux = aux + " " + it.key + "=\"" + it.value +"\""}
            return aux
        }
    val isSelfClosing:Boolean
        get() {
            return childrens.isEmpty() && plainText == ""
        }
    val xmlBegginerTag: String
        get() {
            return if (isSelfClosing) {
                "<$fullName/>"
            } else {
                "<$fullName>"
            }
        }
    val xmlEndTag: String
        get() {
            return if (!isSelfClosing) {
                "</$name>"
            } else {
                ""
            }
        }

    fun addAttribute(attribute: String, value: String){
        if (attributesMap == null) {
            // Create a new HashMap and add the key-value pair
            attributesMap = hashMapOf(attribute to value)
        } else {
            // Add the key-value pair to the existing HashMap
            attributesMap!![attribute] = value
        }
    }
    fun removeAttribute(attribute: String){
        if(attributesMap?.containsKey(attribute) == true)
            attributesMap?.remove(attribute)
        else
            throw NoSuchAttributeException(""+this.name+" does not have such attribute")
    }
    fun editAttribute(attribute: String, newValue: String){
        if(attributesMap?.containsKey(attribute) == true)
            attributesMap?.put(attribute, newValue)
        else
            throw NoSuchAttributeException(""+this.name+" does not have such attribute")
    }
    fun addAllAttributes(attributes: HashMap<String, String>){
        if (attributesMap == null) {
            // Create a new HashMap and add the key-value pair
            attributesMap = attributes
        } else {
            // Add the key-value pair to the existing HashMap
            attributesMap!!.putAll(attributes)
        }
    }
    fun removeAllAttributes(accept: (String, String) -> Boolean){
        attributesMap?.forEach{ (key, value) ->
            if(accept(key, value))
                attributesMap!!.remove(key)
        }
    }
    fun editAllAttributes(accept: (String, String) -> Boolean, newValue: String){
        attributesMap?.forEach{ (key, value) ->
            if(accept(key, value))
                attributesMap!![key] = newValue
        }
    }
    fun getAttributes(accept: (String, String) -> Boolean =  { _, _ -> true }): HashMap<String, String> {
        val filteredAttributes = HashMap<String, String>()
        attributesMap?.forEach { (key, value) ->
            if (accept(key, value)) {
                filteredAttributes[key] = value
            }
        }
        return filteredAttributes
    }
    fun accept(v: XMLVisitor) {
        v.visit(this)
        childrens.forEach {
            it.accept(v)
        }
        v.endVisit(this)
    }
}