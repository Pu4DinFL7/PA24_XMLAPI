import java.io.File
import java.io.PrintWriter
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation


@Target(AnnotationTarget.CLASS)
annotation class XMLName(val name: String)
@Target(AnnotationTarget.PROPERTY)
annotation class EntityXML
@Target(AnnotationTarget.PROPERTY)
annotation class XMLIgnore

/*
@Target(AnnotationTarget.PROPERTY)
annotation class XMLString(val clazz: KClass<out >)

*/

private fun validateName(entityName: String, variableName: String) {

    // Validate entity name to ensure it doesn't contain special characters
    if (!entityName.matches(Regex("[a-zA-Z0-9_]+"))) {
        throw IllegalArgumentException("$variableName can only contain alphanumeric characters, underscores, and hyphens.")
    }
}
data class XMLDocument(
    private val rootEntity: XMLEntity,
    var version:String? = "1.0",
    var encoding:String? = "UTF-8"
){

    val specifications: String
        get() {
            return "<?xml version=\"$version\" encoding=\"$encoding\"?>"
        }

    fun toTree(): String {
        val rootEntity = getRootEntity()
        val tree = XMLToTree()
        rootEntity.accept(tree)
        return tree.collectedText.trim()
    }

    fun getRootEntity(): XMLEntity {
        return rootEntity
    }

    fun addAttributeVisitor(key:String, value:String, accept: (String) -> Boolean){
        validateName(key, "Attribute Name")
        validateName(value, "Attribute Value")

        val attribute = XMLAttributeOperator(key,value, XMLAttributeOperator.Operation.ADD, accept)
        getRootEntity().accept(attribute)
    }

    fun removeAttributeVisitor(key:String, accept: (String) -> Boolean){
        validateName(key, "Attribute Name")
        val attribute = XMLAttributeOperator(key, null, XMLAttributeOperator.Operation.REMOVE, accept)
        getRootEntity().accept(attribute)
    }

    fun editAttributeVisitor(key:String, value: String, accept: (String) -> Boolean){
        validateName(key, "Attribute Name")
        validateName(value, "Attribute Value")

        val attribute = XMLAttributeOperator(key, value, XMLAttributeOperator.Operation.EDIT, accept)
        getRootEntity().accept(attribute)
    }

    fun addEntityVisitor(entityName:String,accept: (String) -> Boolean){
        validateName(entityName, "Entity Name")
        val entity = XMLEntityOperator(null, entityName, XMLEntityOperator.Operation.ADD,accept)
        getRootEntity().accept(entity)
    }

    fun removeEntityVisitor(accept: (String) -> Boolean){
        val entity = XMLEntityOperator(null,null, XMLEntityOperator.Operation.REMOVE, accept)
        getRootEntity().accept(entity)
        entity.removeFromList()
    }

    fun editEntityVisitor(newName:String, accept: (String) -> Boolean){
        validateName(newName, "Entity Name")
        val entity = XMLEntityOperator(newName,null, XMLEntityOperator.Operation.EDIT, accept)
        getRootEntity().accept(entity)
    }

    fun getEntities(accept: (XMLEntity) -> Boolean): List<XMLEntity>{
        val entitiesVisitor = XMLEnitityGetter(accept)
        getRootEntity().accept(entitiesVisitor)
        return entitiesVisitor.entities
    }

    fun toXML(name: String, savePath: String = "${name}.xml"){
        validateName(name, "Entity Name")

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
                for (child in entity.children) {
                    if (child.entityName == part) {
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

interface XMLVisitor{
    fun visit(entity: XMLEntity)
    fun endVisit(entity: XMLEntity){}
}

class XMLToTree :XMLVisitor{
    var collectedText:String = ""
    private var indentationLevel: Int = 0

    override fun visit(entity: XMLEntity) {
        if(indentationLevel>0)
            collectedText += "|".repeat(indentationLevel)+ "-"+ entity.entityName + "\n"
        else
            collectedText += entity.entityName + "\n"
        indentationLevel++
    }

    override fun endVisit(entity: XMLEntity) {
        indentationLevel--
    }
}
class XMLEntityOperator(
    private val newName: String?,
    private val entityName: String?,
    private val operation: Operation,
    private val accept: (String) -> Boolean
): XMLVisitor{
    private var listToRemove: MutableList<XMLEntity> = mutableListOf()
    init{
        if(operation == Operation.ADD){
            if(entityName == null)
                throw IllegalArgumentException("Missing entity name for operation add")
        }
        if(operation ==Operation.EDIT){
            if(newName == null){
                throw IllegalArgumentException("Missing parameters to conclude the edit or remove operation")
            }
        }
    }
    enum class Operation{
        ADD, EDIT, REMOVE
    }
    override fun visit(entity: XMLEntity) {
        if(accept(entity.fullName)){
            if(operation == Operation.ADD){
                if (entityName != null) {
                    XMLEntity(entityName, entity)
                }
            }
            if(operation == Operation.REMOVE){
                listToRemove.add(entity)

            }
            if(operation == Operation.EDIT){
                if (newName != null) {
                    entity.entityName = newName
                }
            }
        }
    }
    fun removeFromList(){
        listToRemove.forEach { a -> a.parent?.children?.remove(a) }
        println("Lista a remover" + listToRemove)
    }
}

class XMLAttributeOperator(
    private val key: String,
    private val value: String?,
    private val operation: Operation,
    private val accept: (String) -> Boolean
): XMLVisitor {
    enum class Operation{
        ADD,EDIT,REMOVE
    }
    override fun visit(entity: XMLEntity) {
        if (accept(entity.fullName)) {
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

class XMLEnitityGetter(val accept: (XMLEntity) -> Boolean) : XMLVisitor {
    var entities:MutableList<XMLEntity> = mutableListOf<XMLEntity>()
    override fun visit(entity: XMLEntity) {
        if(accept(entity))
            entities.add(entity)
    }
}

data class XMLEntity(
    private var name: String,
    var parent: XMLEntity? = null,
    private var attributesMap: HashMap<String,String>? = null,
    private var plainText:String? = ""
) {

    val children = mutableListOf<XMLEntity>()
    init{
        validateName(name, "Entity Name")
        parent?.children?.add(this)
    }

    constructor(obj: Any, parent: XMLEntity? = null) : this(obj::class.simpleName ?: throw IllegalArgumentException("Cannot be null"), parent) {
        val clazz = obj::class

        if (clazz.findAnnotation<XMLName>() != null) {
            entityName = clazz.findAnnotation<XMLName>()!!.name
            //println(" DASSSSSSSS AUTO " + clazz.findAnnotation<XMLName>()!!.name)
        }

        for (property in clazz.declaredMemberProperties) {
            println("aa " + property)
            val value = property.call(obj)

            if (property.findAnnotation<XMLIgnore>() != null) {
                // println("Property ${property.name} is ignored due to XMLIgnore annotation")
                continue
            }
            // println("Processing property: ${property.name}")

            when {
                property.findAnnotation<EntityXML>() != null && value != null -> {
                    XMLEntity(value, this)
                    println("Im coming for you bowser " + value.toString())
                }

                else -> {
                    if (value != null) {
                        addAttribute(property.name, value.toString())
                        // println("IT'SSS MEEEEEEEEE MARIO " + value.toString())
                    }
                }
            }
        }
    }

    var entityName: String
        get(){
            return name
        }
        set(value){
            validateName(value, "Entity Name")
            name=value
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
            return children.isEmpty() && plainText == ""
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
       /* else
            throw NoSuchAttributeException(""+this.name+" does not have such attribute")*/
    }
    fun editAttribute(attribute: String, newValue: String){
        if(attributesMap?.containsKey(attribute) == true)
            attributesMap?.put(attribute, newValue)
        /*else
            throw NoSuchAttributeException(""+this.name+" does not have such attribute")*/
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
        children.forEach {
            it.accept(v)
        }
        v.endVisit(this)
    }


    override fun toString(): String {
       return fullName
    }
}