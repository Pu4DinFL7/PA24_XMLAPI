import java.io.File
import java.io.PrintWriter
import java.util.*
import kotlin.collections.LinkedHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.*

/**
 * Annotation to specify the XML name of a class.
 *
 * @property name The XML name of the class.
 */
@Target(AnnotationTarget.CLASS)
annotation class XMLName(val name: String)

/**
 * Annotation to specify that a property should be translated into an Entity and added as a child.
 * If the annotation is attached to a List then it will create Entities for each Object on the list.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class EntityXML

/**
 * Annotation to specify that a property should be ignored in the XML.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class XMLIgnore

/**
 * Annotation for specifying a [StringEdit] class to be used for editing a string property.
 *
 * @property clazz The [KClass] of the [StringEdit] implementation to be used.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class XMLString(val clazz: KClass<out StringEdit>)

/**
 * Annotation for specifying an [EntityAdapter] class to be used for adapting an entity.
 *
 * @property clas The [KClass] of the [EntityAdapter] implementation to be used.
 */
@Target(AnnotationTarget.CLASS)
annotation class XMLAdapter(val clas: KClass<out EntityAdapter>)


/**
 * Validates that the given string starts with a letter or underscore and contains only alphanumeric characters, underscores, periods and hyphens.
 *

 *
 * @param stringToTest The string to be validated.
 * @param variableName The name of the variable being validated, used in the exception message.
 * @throws IllegalArgumentException if the string contains invalid characters.
 */
private fun validateString(stringToTest: String, variableName: String){
    if (!stringToTest.matches(Regex("^[_a-zA-ZÀ-ÖØ-öø-üÀ-Üà-öø-ÿº0-9.-]*\$"))) {
        throw IllegalArgumentException("$variableName must start with a letter or underscore and can only contain alphanumeric characters, underscores, periods and hyphens.")
    }
}

/**
 * Escapes special characters in the input string to ensure it is safe for use in XML.
 * Replaces characters such as '&', '<', '>', '"', and "'" with their corresponding XML entities.
 *
 * @param stringToEscape The input string to be escaped.
 * @return The escaped string.
 */
private fun escapeText(stringToEscape: String): String{
    return stringToEscape.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}

/**
 * Represents an XML document with a root entity, version, and encoding.
 * Provides functionalities for manipulating XML entities and attributes
 * using the Visitor Pattern Design.
 * Supports visualization of the entities and also exporting the document to an XML file.
 *
 * @property _rootEntity The root entity of the XML document.
 * @property _version The version of the XML document, default is "1.0".
 * @property _encoding The encoding of the XML document, default is "UTF-8".
 *
 *  @constructor Creates an XML document with the specified root entity, version, and encoding.
 *  @throws IllegalArgumentException if the provided XML version or encoding is invalid.
 *
 */
data class XMLDocument(
    private var _rootEntity: XMLEntity,
    private var _version: String? = "1.0",
    private var _encoding: String? = "UTF-8"
){

    init {
        require(isValidVersion(_version)) { "Invalid XML version: $_version" }
        require(isValidEncoding(_encoding)) { "Invalid XML encoding: $_encoding" }
    }

    /**
     * Checks if the provided XML version is valid.
     *
     * @param version The XML version to validate.
     * @return True if the version is "1.0" or "1.1", false otherwise.
     */
    private fun isValidVersion(version: String?): Boolean {
        return version == "1.0" || version == "1.1"
    }

    /**
     * Checks if the provided XML encoding is valid.
     *
     * @param encoding The XML encoding to validate.
     * @return True if the encoding is one of the commonly used encodings, false otherwise.
     */
    private fun isValidEncoding(encoding: String?): Boolean {
        val validEncodings = listOf("UTF-8", "UTF-16", "ISO-8859-1", "US-ASCII")
        return encoding != null && validEncodings.contains(encoding.uppercase(Locale.getDefault()))
    }

    /**
     * The version of the XML document.
     */
    var version: String?
        get() = _version
        set(value) {
            require(isValidVersion(value)) { "Invalid XML version: $value" }
            _version = value
        }

    /**
     * The encoding of the XML document.
     */
    var encoding: String?
        get() = _encoding
        set(value) {
            require(isValidEncoding(value)) { "Invalid XML encoding: $value" }
            _encoding = value
        }

    /**
     * Returns the XML declaration as a string.
     */
    val specifications: String
        get() {
            return "<?xml version=\"$version\" encoding=\"$encoding\"?>"
        }

    /**
     * The root entity of the XML document.
     */
    val rootEntity: XMLEntity
        get() = _rootEntity


    /**
     * Converts the XML document to a tree representation.
     *
     * @return The tree representation of the XML document.
     */
    fun toTree(): String {
        val tree = XMLToTree()
        rootEntity.accept(tree)
        return tree.toTreeString().trim()
    }

    /**
     * Adds an attribute to all XML entities that meet the provided acceptance criteria.
     * @param name The name of the attribute to add.
     * @param value The value of the attribute to add.
     * @param accept A lambda function that defines the acceptance criteria for the entities by comparing entity.fullName.
     *
     */
    fun addAttribute(name:String, value:String, accept: (String) -> Boolean){
        validateString(name, "Attribute Name")

        val attribute = XMLAttributeOperator(name,escapeText(value), XMLAttributeOperator.Operation.ADD, accept)
        rootEntity.accept(attribute)
    }

    /**
     * Removes an attribute to all XML entities that meet the provided acceptance criteria.
     * @param name The name of the attribute to remove.
     * @param accept A lambda function that defines the acceptance criteria for the entities by comparing entity.fullName.
     *
     */
    fun removeAttribute(name:String, accept: (String) -> Boolean){
        validateString(name, "Attribute Name")
        val attribute = XMLAttributeOperator(name, null, XMLAttributeOperator.Operation.REMOVE, accept)
        rootEntity.accept(attribute)
    }

    /**
     * Edits an attribute to all XML entities that meet the provided acceptance criteria.
     * @param name The name of the attribute to edit.
     * @param newValue The new value of the attribute.
     *
     * @param accept A lambda function that defines the acceptance criteria for the entities by comparing entity.fullName.
     *
     */
    fun editAttribute(name:String, newValue: String, accept: (String) -> Boolean){
        validateString(name, "Attribute Name")

        val attribute = XMLAttributeOperator(name, escapeText(newValue), XMLAttributeOperator.Operation.EDIT, accept)
        rootEntity.accept(attribute)
    }


    /**
     * Adds a new entity as a child to all XML entities that match the specified criteria.
     * @param entityName The name of the entity to add as a child.
     * @param accept A lambda function that defines the acceptance criteria for the parent entities by comparing entity.fullName.
     */
    fun addEntity(entityName:String, accept: (String) -> Boolean){
        validateString(entityName, "Entity Name")
        val entity = XMLEntityOperator(null, entityName, XMLEntityOperator.Operation.ADD,accept)
        rootEntity.accept(entity)
    }

    /**
     * Removes all the entities that match the specified criteria.
     * @param accept A lambda function that defines the acceptance criteria for entities by comparing entity.fullName.
     */
    fun removeEntity(accept: (String) -> Boolean){
        val entity = XMLEntityOperator(null,null, XMLEntityOperator.Operation.REMOVE, accept)
        rootEntity.accept(entity)
        entity.removeFromList()
    }

    /**
     * Renames all the entities that match the specified criteria to the new name.
     * @param newName The new name for the entities
     * @param accept A lambda function that defines the acceptance criteria for entities by comparing entity.fullName.
     */
    fun renameEntity(newName:String, accept: (String) -> Boolean){
        validateString(newName, "Entity Name")
        val entity = XMLEntityOperator(newName,null, XMLEntityOperator.Operation.EDIT, accept)
        rootEntity.accept(entity)
    }

    /**
     * Retrieves entities from the XML document based on acceptance criteria.
     * @param accept The acceptance criteria for the entities.
     * @return A list of entities matching the acceptance criteria.
     */
    fun getEntities(accept: (XMLEntity) -> Boolean = {true}): List<XMLEntity>{
        val entitiesVisitor = XMLEntityGetter(accept)
        rootEntity.accept(entitiesVisitor)
        return entitiesVisitor.entities
    }

    /**
     * Visits the XML document with a custom XMLVisitor.
     * @param visitor The custom XMLVisitor to visit the XML document.
     */
    fun visitDocument(visitor: XMLVisitor){
        rootEntity.accept(visitor)
    }

    /**
     * Converts the XML document to a string and saves it to a file.
     *
     * @param name The name of the XML document.
     * @param savePath The path where the XML document should be saved. Default is "$name.xml".
     */
    fun toXML(name: String, savePath: String = "${name}.xml"){
        val xmlCollector = XMLTextCollector()
        xmlCollector.collectedText = specifications
        rootEntity.accept(xmlCollector)
        val file = File(savePath)
        PrintWriter(file).use { out ->
            out.write(xmlCollector.collectedText)
        }

    }

    /**
     * Queries the XML document using an XPath-like expression.
     *
     * @param expression The XPath-like expression to query the XML document.
     * @return A list of strings representing the entities that match the query.
     */
    fun queryXPath(expression: String): List<String> {
        val entities = mutableListOf<String>()
        val parts = expression.split("/")

        var currentEntities = listOf(rootEntity)

        for (part in parts) {
            val newEntities = mutableListOf<XMLEntity>()
            for (entity in currentEntities) {
                for (child in entity.children) {
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

/**
 * Interface defining methods for visiting XML entities.
 */
interface XMLVisitor{

    /**
     * Visits the given XML entity.
     * @param entity The XML entity to visit.
     */
    fun visit(entity: XMLEntity)

    /**
     * Performs actions at the end of visiting the XML entity.
     * @param entity The XML entity for which the visit is ending.
     */
    fun endVisit(entity: XMLEntity){}
}

/**
 * Converts XML to a tree structure.
 */
private class XMLToTree : XMLVisitor {
    private val stringBuilder = StringBuilder()
    private var indentationLevel = 0

    override fun visit(entity: XMLEntity) {
        appendIndentation(entity)
        stringBuilder.append(entity.name)
        stringBuilder.append("\n")
        indentationLevel++
    }

    override fun endVisit(entity: XMLEntity) {
        indentationLevel--
        if (indentationLevel < 0) {
            indentationLevel = 0
        }
    }

    private fun appendIndentation(entity: XMLEntity) {
        if (indentationLevel > 0) {
            stringBuilder.append("│   ".repeat(indentationLevel-1))

            var isLastChild = false
            if (entity.parent != null) {
                val siblings = entity.parent!!.children
                isLastChild = siblings.indexOf(entity) == siblings.size - 1
            }
            stringBuilder.append(if (isLastChild)  "└── " else "├── ")
        }
    }

    fun toTreeString(): String {
        return stringBuilder.toString()
    }
}

/**
 * Operator for manipulating XML entities.
 */
 private class XMLEntityOperator(
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

    /**
     * Enum defining different operations for XMLEntityOperator.
     */
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
                    entity.name = newName
                }
            }
        }
    }

    /**
     * Removes entities from the list.
     */
    fun removeFromList(){
        listToRemove.forEach { a -> a.parent?.children?.remove(a) }
    }
}

/**
 * Operator for manipulating XML attributes.
 */
 private class XMLAttributeOperator(
    private val name: String,
    private val value: String?,
    private val operation: Operation,
    private val accept: (String) -> Boolean
): XMLVisitor {

    /**
     * Enum defining different operations for XMLAttributeOperator.
     */
    enum class Operation{
        ADD,EDIT,REMOVE
    }
    override fun visit(entity: XMLEntity) {
        if (accept(entity.fullName)) {
            if(operation == Operation.ADD) {
                if (value != null) {
                    entity.addAttribute(name, value)
                }
            }
            if(operation == Operation.REMOVE){
                entity.removeAttribute(name)
            }

            if(operation == Operation.EDIT){
                if (value != null) {
                    entity.editAttribute(name,value)
                }
            }
        }
    }
}

/**
 * Collects text from XML entities.
 */
 private class XMLTextCollector : XMLVisitor {
    var collectedText:String = ""
    private var indentationLevel: Int = 0
    override fun visit(entity: XMLEntity) {

        collectedText += "\n"+"\t".repeat(indentationLevel)
        indentationLevel++

        if(entity.plainText != "")
            collectedText += entity.xmlBegginerTag + entity.plainText
        else
            collectedText += entity.xmlBegginerTag
    }

    override fun endVisit(entity: XMLEntity) {
        indentationLevel--
        if(!entity.isSelfClosing)
            if(entity.plainText != "") {
                collectedText += entity.xmlEndTag
            }else
                collectedText += "\n" +"\t".repeat(indentationLevel)+ entity.xmlEndTag

    }
}

/**
 * Retrieves XML entities based on acceptance criteria.
 */
 private class XMLEntityGetter(val accept: (XMLEntity) -> Boolean) : XMLVisitor {
    var entities:MutableList<XMLEntity> = mutableListOf<XMLEntity>()
    override fun visit(entity: XMLEntity) {
        if(accept(entity))
            entities.add(entity)
    }
}



/**
 * Represents an XML entity with a name, parent, attributes, and plain text content.
 * @property name The name of the entity.
 * @property parent The parent entity. Default is null.
 * @property attributesMap A map storing the attributes of the entity. Default is null.
 * @property plainText The plain text content of the entity. Default is an empty string.
 * @property children A list of child entities.
 * @constructor Creates an XMLEntity with the provided name and parent, validates the name, and adds itself to the parent's children OR
 * Creates an XMLEntity using an object from a class through reflection and annotations.
 */
data class XMLEntity(
    private var _name: String,
    var parent: XMLEntity? = null,
    private var attributesMap: LinkedHashMap<String,String>? = null,
    private var _plainText:String? = ""
) {

    /**
     * Initializes the list of child entities and adds the current entity to its parent's children.
     */
    val children = mutableListOf<XMLEntity>()
    init{
        validateString(_name, "Entity Name")
        _plainText = _plainText?.let { escapeText(it) }
        parent?.children?.add(this)
    }

    /**
     * Constructs an XMLEntity from another object, populating attributes based on annotations and recursively creating child entities.
     * @param obj The object to construct the entity from.
     * @param parent The parent entity. Default is null.
     */
    constructor(obj: Any, parent: XMLEntity? = null) : this(obj::class.simpleName ?: throw IllegalArgumentException("Cannot be null"), parent) {
        val clazz = obj::class

        if (clazz.findAnnotation<XMLName>() != null) {
            name = clazz.findAnnotation<XMLName>()!!.name
        }

        for (property in clazz.declaredMemberProperties) {
            val value = property.call(obj)

            when {
                property.findAnnotation<XMLIgnore>() != null ->{
                    continue
                }

                property.findAnnotation<EntityXML>() != null && value != null -> {
                    if(value is List<*>){
                        value.forEach {c ->
                            if (c != null) {
                                XMLEntity(c, this)
                            }
                        }
                    }else {
                        XMLEntity(value, this)
                    }
                }

                property.findAnnotation<XMLString>() != null ->{
                    val str = property.findAnnotation<XMLString>()
                    val aux = str?.clazz?.createInstance()?.change(value.toString())
                    if (aux != null) {
                        addAttribute(property.name, aux)
                    }
                }

                else -> {
                    if (value != null) {
                        addAttribute(property.name, value.toString())
                    }
                }
            }

            if(clazz.findAnnotation<XMLAdapter>() != null){
                val adapterAnnotation = clazz.findAnnotation<XMLAdapter>()
                val adapter = adapterAnnotation?.clas?.createInstance()
                adapter?.adapt(this)

            }
        }
    }

    /**
     * The name of the entity.
     */
    var name: String
        get(){
            return _name
        }
        set(value){
            validateString(value, "Entity Name")
            _name=value
        }

    /**
     * The plain text content of the entity.
     */
    var plainText: String?
        get() = _plainText
        set(value) {
            _plainText = value?.let { escapeText(it) }
        }

    /**
     * The full name of the entity including attributes.
     */
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

    /**
     * Adds an attribute to the entity.
     * @param attribute The name of the attribute to add.
     * @param value The value of the attribute to add.
     * @throws IllegalArgumentException if the attribute already exists in the entity.
     */
    fun addAttribute(attribute: String, value: String){
        validateString(attribute, "Attribute Name")
        if (attributesMap == null) {
            // Create a new LinkedHashMap and add the key-value pair
            attributesMap = linkedMapOf(attribute to escapeText(value))
        } else {
            if(attributesMap!!.containsKey(attribute))
                throw IllegalArgumentException("$attribute already exists! To change its value, use editAttribute.")

            // Add the key-value pair to the existing LinkedHashMap
            attributesMap!![attribute] = escapeText(value)
        }
    }

    /**
     * Removes an attribute from the entity.
     * @param attribute The name of the attribute to remove.
     */
    fun removeAttribute(attribute: String){
        if(attributesMap?.containsKey(attribute) == true)
            attributesMap?.remove(attribute)
    }

    /**
     * Edits an attribute of the entity.
     * @param attribute The name of the attribute to edit.
     * @param newValue The new value for the attribute.
     */
    fun editAttribute(attribute: String, newValue: String){

        if(attributesMap?.containsKey(attribute) == true)
            attributesMap?.put(attribute, escapeText(newValue))

    }

    /**
     * Adds multiple attributes to the entity.
     * @param attributes A map containing the attributes to add.
     * @throws IllegalArgumentException if any attribute already exists in the entity.
     */
    fun addAllAttributes(attributes: LinkedHashMap<String, String>){

        if (attributesMap == null) {
            attributesMap = LinkedHashMap()
        }
        for ((key, value) in attributes) {
            if(attributesMap!!.containsKey(key))
                throw IllegalArgumentException("$key already exists! To change its value, use editAttribute.")
            validateString(key, "Attribute Name")
            attributesMap!![key] = escapeText(value)
        }
    }

    /**
     * Removes attributes from the entity based on the acceptance criteria provided.
     * @param accept A lambda function defining the acceptance criteria for attributes.
     */
    fun removeAllAttributes(accept: (String, String) -> Boolean){
        val keysToRemove = mutableListOf<String>()
        attributesMap?.forEach { (key, value) ->
            if (accept(key, value)) {
                keysToRemove.add(key)
            }
        }

        keysToRemove.forEach { key ->
            attributesMap?.remove(key)
        }
    }

    /**
     * Edits attributes of the entity based on the acceptance criteria provided.
     * @param accept A lambda function defining the acceptance criteria for attributes.
     * @param newValue The new value for the attributes.
     */
    fun editAllAttributes(accept: (String, String) -> Boolean, newValue: String){
        attributesMap?.forEach{ (key, value) ->
            if(accept(key, value))
                attributesMap!![key] = escapeText(newValue)
        }
    }

    /**
     * Retrieves attributes of the entity based on the acceptance criteria provided.
     * @param accept A lambda function defining the acceptance criteria for attributes. Default is to accept all attributes.
     * @return A LinkedHashMap containing the filtered attributes.
     */
    fun getAttributes(accept: (String, String) -> Boolean =  { _, _ -> true }): LinkedHashMap<String, String> {
        val filteredAttributes = LinkedHashMap<String, String>()
        attributesMap?.forEach { (key, value) ->
            if (accept(key, value)) {
                filteredAttributes[key] = value
            }
        }
        return filteredAttributes
    }

    /**
     * Clears all attributes associated with this XML entity.
     * If there are no attributes, this function has no effect.
     */
    fun clearAttributes() {
        attributesMap?.clear()
    }

    /**
     * Accepts a visitor to visit the entity and its children.
     * @param visitor The XMLVisitor to accept.
     */
    fun accept(visitor: XMLVisitor) {
        visitor.visit(this)
        children.forEach {
            it.accept(visitor)
        }
        visitor.endVisit(this)
    }

    /**
     * Returns the full name of the entity.
     * @return The full name of the entity.
     */
    override fun toString(): String {
       return fullName
    }
}


/**
 * Interface for editing a string value.
 */
interface StringEdit {
    /**
     * Changes the given string value.
     *
     * @param value The string value to be changed.
     * @return The changed string value.
     */
    fun change(value: String): String
}

/**
 * Interface for adapting an [XMLEntity].
 */
interface EntityAdapter {
    /**
     * Adapts the given [XMLEntity].
     *
     * @param entity The [XMLEntity] to be adapted.
     * @return The adapted [XMLEntity].
     */
    fun adapt(entity: XMLEntity): XMLEntity
}

/**
 * Creates an XML document using the specified builders for document structure and content.
 *
 * @param buildToCreateDoc A lambda function for building the structure of the XML document using an [XMLDocumentBuilder].
 * @param documentBuild A lambda function for further building the content of the created [XMLDocument]. Default is an empty function.
 * @return The created [XMLDocument].
 */
fun xmlDocument(buildToCreateDoc: XMLDocumentBuilder.() -> Unit, documentBuild: XMLDocument.() -> Unit = {}): XMLDocument {
    val builder = XMLDocumentBuilder()
    builder.buildToCreateDoc()
    val document = builder.buildDocument()
    document.apply(documentBuild)
    return document
}

/**
 * Builder class for creating an [XMLDocument].
 */
class XMLDocumentBuilder {
    private var rootEntity: XMLEntity? = null
    private var version: String? = "1.0"
    private var encoding: String? = "UTF-8"

    /**
     * Sets the version of the XML document.
     *
     * @param version The version of the XML document.
     */
    fun version(version: String) {
        this.version = version
    }

    /**
     * Sets the encoding of the XML document.
     *
     * @param encoding The encoding of the XML document.
     */
    fun encoding(encoding: String) {
        this.encoding = encoding
    }

    /**
     * Sets the root entity of the XML document.
     *
     * @param name The name of the root entity.
     * @param build A lambda function for building the root entity.
     * @throws IllegalArgumentException If the root entity is already set.
     */
    fun root(name: String, build: XMLEntity.() -> Unit = {}) {
        if(rootEntity != null)
            throw IllegalArgumentException("This document already has a root entity!")
        rootEntity = XMLEntity(name).apply {
            build(this)
        }
    }

    /**
     * Builds and returns the XML document.
     *
     * @return The created [XMLDocument].
     * @throws IllegalArgumentException If the root entity is not specified.
     */
    fun buildDocument(): XMLDocument {
        requireNotNull(rootEntity) { "Root entity must be specified" }
        return XMLDocument(rootEntity!!, version, encoding)
    }
}

/**
 * Creates an [XMLEntity] with the specified name and builder.
 *
 * @param name The name of the XML entity.
 * @param build A lambda function for building the XML entity.
 * @return The created [XMLEntity].
 */
fun xmlEntity(name: String, build: XMLEntity.() -> Unit = {}) =
    XMLEntity(name).apply {
        build(this)
    }

/**
 * Adds a child entity to this [XMLEntity].
 *
 * @param name The name of the child entity.
 * @param build A lambda function for building the child entity.
 * @return The created child [XMLEntity].
 */
fun XMLEntity.child(name: String, build: XMLEntity.() -> Unit = {}) =
    XMLEntity(name, this).apply {
        build(this)
    }

/**
 * Retrieves the entities with the specified name from the [XMLDocument].
 *
 * @param name The name of the entities to retrieve.
 * @return A list of [XMLEntity] objects with the specified name.
 */
operator fun XMLDocument.get(name: String): List<XMLEntity> =
    getEntities { it.name == name }

/**
 * Retrieves the attributes with the specified name from each [XMLEntity] in this list.
 *
 * @param name The name of the attributes to retrieve.
 * @return A list of maps containing the attributes with the specified name.
 */
operator fun List<XMLEntity>.get(name: String): List<LinkedHashMap<String,String>>{
    val list = mutableListOf<LinkedHashMap<String,String>>()
    forEach { entity ->
        list.add(entity.getAttributes() { key, _ -> key == name }) }
    return list
}

/**
 * Retrieves the attributes with the specified name from this [XMLEntity].
 *
 * @param name The name of the attributes to retrieve.
 * @return A map containing the attributes with the specified name.
 */
operator fun XMLEntity.get(name: String): LinkedHashMap<String, String> =
    getAttributes { key, _ -> key == name }


/**
 * Retrieves the child entity with the specified name from this [XMLEntity].
 *
 * @param name The name of the child entity to retrieve.
 * @return The child [XMLEntity] with the specified name.
 */
operator fun XMLEntity.div(name: String): XMLEntity =
    children.find { it.name == name } as XMLEntity