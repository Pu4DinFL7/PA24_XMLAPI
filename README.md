
<h1 align="center">
  XML Creation API <br>
  Programação Avançada @ ISCTE 2023/2024
</h1>

# Introduction

The XML Creation API is a Kotlin library designed to facilitate the creation, manipulation, and serialization of XML documents. It provides a set of classes and functions that allow developers to build XML documents programmatically, define XML entities, attributes, and relationships, and export the resulting XML to files.

Features

  - **Simple and Intuitive:** Easily create XML documents using Kotlin syntax.
  
  - **Validation:** Validate XML version, encoding, and string formats to ensure compliance.
  
  - **Visitor Pattern:** Utilize the Visitor Pattern design for traversing and manipulating XML entities.
  
  - **Visualization:** You can visualize the XML document you are creating through the `toTree` method.
  
  - **Querying:** Perform XPath-like queries to retrieve specific entities from the document.

  - **Programmatic XML Document Creation:** Developers can create XML documents programmatically using Kotlin DSL provided by the API.

  - **Annotation-Based XML Entity Construction:** The API supports the creation of XML entities from Kotlin classes using reflection and annotations. This feature enables seamless translation of Kotlin objects into XML entities.

  - **Flexible XML Entity Manipulation:** Users can manipulate XML entities by adding, removing, or editing attributes and child entities using lambda functions to specify acceptance criteria.

  - **XML Serialization:** The API allows serialization of XML documents to files in XML format, enabling storage and transmission of XML data.

# `XML Entity`
The XMLEntity class represents an XML entity with a name, parent, attributes, and plain text content. It supports attribute manipulation, child entity creation, and visitor pattern acceptance.
Creating one can be done through one of two constructors.

Simple Constructor<br> 
This constructor takes a string as a parameter to name the entity. Optionally, you can specify a parent XML entity. This method provides a straightforward approach to entity creation.
```kotlin
    val europe = XMLEntity("Europe")
    val portugal = XMLEntity("Portugal", europe)

    //This code will make an XMLEntity "Portugal" which is a child of the XMLEntity "Europe"
```

Secondary Constructor<br>
This constructor accepts any object from a Kotlin class. It then translates that object to an XML entity using Reflection and Annotation. This method offers flexibility and convenience, especially when dealing with complex data structures.

```kotlin
    data class Person(
        val name: String,
        val age: Int,
        @XMLIgnore val password: String,
        @EntityXML val address: Address
    )

    data class Address(
        val street: String,
        val number: Int,
        val city: String
    )

    val address = Address("Main St", 123, "Springfield")
    val personClass = Person("John Doe", 30, "secret", address)
    val personEntity = XMLEntity(personClass)

    //This will create an XMLEntity "Person" that has attributes and a child entity "Address"
```

## Annotations

The XML Creation API provides annotations to customize the behavior of XML entity generation when using the secondary constructor. <br>
These annotations offer fine-grained control over how Kotlin class properties are translated into XML attributes or elements. <br>
If no annotation is attached to a given property then it is considered to be an XML Attribute by default.

#### `@XMLName(name: String)`

- **Description:** Specifies the name that will be given to the XML Entity.
- **Usage:** Apply to a class.

#### `@EntityXML(name: String)`

- **Description:** Indicates that an object should be translated into an XML entity and added as a child of the annotated entity.
- **Usage:** Apply to a property containing an object or a list of objects. If applied to a list, each object will be translated into an entity and added as a child.


#### `@XMLIgnore`

- **Description:** Instructs the XML entity generation process to ignore the annotated property.
- **Usage:** Apply to a property within a Kotlin class.

#### `@XMLString(clazz: KClass<out StringEdit>)`

- **Description:** Specifies a [StringEdit](#stringedit) implementation to be used for editing a string property.
- **Usage:** Apply to a string property. Provide the class of the [StringEdit](#stringedit) implementation as the parameter.

#### `@XMLAdapter(clas: KClass<out EntityAdapter>)`

- **Description:** Specifies an [EntityAdapter](#entityadapter) implementation to be used for adapting an entity.
- **Usage:** Apply to a class. Provide the class of the [EntityAdapter](#entityadapter) implementation as the parameter.

---

#### `StringEdit`

Interface for editing a string value.

- **Method:**
  - `change(value: String): String`: Changes the given string value.

---

#### `EntityAdapter`

Interface for adapting an [XML Entity](#xml-entity).

- **Method:**
  - `adapt(entity: XMLEntity): XMLEntity`: Adapts the given [XML Entity](#xml-entity).
### Notes

    If no annotation is attached to a given property then it is considered to be an XML Attribute by default.
    Annotated properties must be either var or val.
    Annotations are processed only for properties with getter methods.
    Annotation values must be constant expressions.
    Only one annotation per property is allowed.

### XMLEntity Methods
- `addAttribute(attribute: String, value: String)`: Adds an attribute to the XML entity.
- `removeAttribute(attribute: String)`: Removes an attribute from the XML entity.
- `editAttribute(attribute: String, newValue: String)`: Edits the value of an attribute.
- `addAllAttributes(attributes: LinkedHashMap<String, String>)`: Adds a set of attributes to the XML entity.
- `removeAllAttributes(accept: (String, String) -> Boolean)`: Removes attributes based on a criteria function.
- `editAllAttributes(accept: (String, String) -> Boolean, newValue: String)`: Edits attributes based on a criteria function.
- `getAttributes(accept: (String, String) -> Boolean = { _, _ -> true }): LinkedHashMap<String, String>`: Retrieves attributes based on a criteria function.
- `clearAttributes()`: Clears all attributes from the XML entity.
- `accept(visitor: XMLVisitor)`: Accepts a visitor for the XML entity.
- `toString(): String`: Returns a string representation of the XML entity.
**Note:** The criteria function must receive the attribute name and based on that returns a Boolean.

# `XML Document`

The `XMLDocument` class represents an XML document but only has the `rootEntity`, `version` and `encoding` as an attribute, effectively not knowing anything about all the other entities.<br>
It provides functionality, through the visitor design pattern, to create, manipulate, and traverse the XML hierarchy, making it a central component for handling XML data. <br>
Whether you need to construct an XML document from scratch, modify existing elements, or serialize the document to a string, the `XMLDocument` class offers a comprehensive set of methods to achieve these tasks.

To create an XMLDocument you need to give an XMLEntity that will become associated to the document.
To visualize the XMLDocument you can use the toTree method.
```kotlin
val poem = XMLEntity("Poem")
val doc = XMLDocument(poem)

println(doc.toTree())
// This will create a document that has the 'poem' XMLEntity has the 'rootEntity'
// Output
// Poem

```
You can also create several Entities in a hierarchy and then associate the root of the hierarchy to the document

```kotlin
val poem = XMLEntity("Poem")
val fogo = XMLEntity("Amor_é_fogo", poem)
fogo.addAttribute("Author" to "Luís Vaz de Camões")
val doc = XMLDocument(poem)

// This will create a document that will have 'poem' has the 'rootEntity'
// The 'poem' Entity has a child called 'fogo'
// 'fogo' has an attribute called 'Author' and with value 'Luís Vaz de Camões'

println(doc.toTree())
// Output
// Poem
//   └── Amor_é_fogo
```

### Visitors

As explained earlier the XMLDocument only has a reference to one XMLEntity called internally 'rootEntity'.<br>
The XMLDocument is just a representation of which XMLEntity is the first entity and should be used in all the XMLDocument methods.<br>
Every method thus has to implement the Visitor Pattern Design to begin in the 'rootEntity' and propagate through it's children.

The XML Creator API has some built in visitors available that are already being used in the methods. There's also the possibility to create a visitor that must be an [XMLVisitor](#xmlvisitor) implementation

#### `XMLVisitor`

The `XMLVisitor` interface allows for implementing the visitor design pattern, providing methods to visit and end the visit of an [XMLEntity](#xml-entity).

- **Methods**

  - `visit(entity: XMLEntity)`: Visits and performs actions during the visit of the given XML entity.
  - `endVisit(entity: XMLEntity)`: Performs actions at the end of visiting the XML entity.

```kotlin
interface XMLVisitor {

    /**
     * Visits the given XML entity.
     * @param entity The XML entity to visit.
     */
    fun visit(entity: XMLEntity)

    /**
     * Performs actions at the end of visiting the XML entity.
     * @param entity The XML entity for which the visit is ending.
     */
    fun endVisit(entity: XMLEntity) {}
}
```

### XMLDocument Methods

- `isValidVersion(version: String?): Boolean`: Checks if the provided XML version is valid.
- `isValidEncoding(encoding: String?): Boolean`: Checks if the provided XML encoding is valid.
- `version: String?`: The version of the XML document. Get and set properties.
- `encoding: String?`: The encoding of the XML document. Get and set properties.
- `specifications: String`: Returns the XML declaration as a string.
- `rootEntity: XMLEntity`: The root entity of the XML document.
- `toTree(): String`: Converts the XML document to a tree representation.
- `addAttribute(name: String, value: String, accept: (String) -> Boolean)`: Adds an attribute to all XML entities that meet the provided acceptance criteria.
- `removeAttribute(name: String, accept: (String) -> Boolean)`: Removes an attribute from all XML entities that meet the provided acceptance criteria.
- `editAttribute(name: String, newValue: String, accept: (String) -> Boolean)`: Edits an attribute of all XML entities that meet the provided acceptance criteria.
- `addEntity(entityName: String, accept: (String) -> Boolean)`: Adds a new entity as a child to all XML entities that match the specified criteria.
- `removeEntity(accept: (String) -> Boolean)`: Removes all the entities that match the specified criteria.
- `renameEntity(newName: String, accept: (String) -> Boolean)`: Renames all the entities that match the specified criteria to the new name.
- `getEntities(accept: (XMLEntity) -> Boolean = { true }): List<XMLEntity>`: Retrieves entities from the XML document based on acceptance criteria.
- `visitDocument(visitor: XMLVisitor)`: Visits the XML document with a custom [XMLVisitor](#xmlvisitor).
- `toXML(name: String, savePath: String = "${name}.xml")`: Converts the XML document to a string and saves it to a file.
- `queryXPath(expression: String): List<String>`: Queries the XML document using an XPath-like expression.

## DSL (Domain Specific Language)

The XML API offers a powerful DSL (Domain Specific Language) that allows for intuitive and expressive creation of XML documents.<br>
This DSL simplifies the process of generating XML structures by providing a Kotlin-based syntax that closely resembles XML markup.
Utilizing this syntax will make the code more readable, productive and flexible!

### Features of the DSL:

1. **Concise Syntax**: The DSL provides a concise syntax for defining XML structures, reducing boilerplate code and enhancing readability.

2. **Type-Safe Constructs**: Leveraging Kotlin's type safety, the DSL ensures that XML elements and attributes are constructed in a type-safe manner, reducing the risk of runtime errors.

3. **Expressive Abstractions**: The DSL offers expressive abstractions for representing XML elements, attributes, and content, allowing developers to focus on the high-level structure of the XML document.

4. **Builder Pattern**: Under the hood, the DSL utilizes the builder pattern to construct XML elements and attributes programmatically, providing flexibility and control over the XML generation process.

### Example Usage:

```kotlin

val doc = xmlDocument({
            version("1.0")
            encoding("UTF-8")
            root("Game_of_Thrones") {
                child("John_Snow"){
                    addAttribute("House", "Stark")
                }
                child("Cersei_Lannister"){
                    addAttribute("House", "Lannister")
                }
                child("Daenerys_Targaryen"){
                    addAttribute("House", "Targaryen")
                }
            }
        }){
            addAttribute("eye-color", "grey"){ entityName -> entityName == "John_Snow House=\"Stark\""}
            println(toTree())
            toXML("GOT")
        }
// Output
// toTree
// Game_of_Thrones
// ├── John_Snow
// ├── Cersei_Lannister
// └── Daenerys_Targaryen

// toXML
// <?xml version="1.0" encoding="UTF-8"?>
// <Game_of_Thrones>
//   <John_Snow House="Stark" eye-color="grey"/>
//   <Cersei_Lannister House="Lannister"/>
//	 <Daenerys_Targaryen House="Targaryen"/>
// </Game_of_Thrones>
```

In ressemblance to the XMLDocument, the DSL also provides syntax for an XML Entity

```kotlin
 val regularShow = xmlEntity("Regular-Show"){
            child("Mordecai"){
                addAttribute("Mordequices", "1")
            }
            child("Rigby"){
                addAttribute("Eggcelent","1")
            }
            addAttribute("Release-date" to "2010")
        }
// This will create a "Regular-Show" entity that will have an attribute "Release-date" and will have two children, each with theire attributes.
```

In addition to this, the DSL also has operators for easy access to entities
``` kotlin
  val jungleAdventure = xmlEntity("Jungle-Adventure") {
    child("Brandy") {
        addAttribute("Species", "Dog")
        addAttribute("Occupation", "Socialite")
    }
    child("MrWhiskers") {
        addAttribute("Species", "Rabbit")
        addAttribute("Occupation", "Adventurer")
        child("Pedro") {
            addAttribute("Species", "Toucan")
            addAttribute("Personality", "Chatterbox")
        }
    }
    addAttribute("Airdate" to "2004")
  }
  jungleAdventure / "MrWhiskers" / "Pedro" // <- this will return a list of XMLEntities with the entity Pedro
  jungleAdventure["Airdate"] //<- this will return the attribute {Airdate=2004}

  // You can also use this in conjuction with one another!
  (jungleAdventure / "MrWhiskers" / "Pedro")["Species"] //<- this will return the attribute {Species=Toucan} of 'Pedro'
```

## Authors
<h1 align="center"><a href="https://github.com/Pu4DinFL7" style="text-decoration:none;"><img src="https://github.com/Pu4DinFL7.png" alt="Flávio Martins" width="100" height="100" style="border-radius: 50%; filter: grayscale(100%); transition: all 0.3s;"></a> <a href="https://github.com/Shrimpo22" style="text-decoration:none;"><img src="https://github.com/Shrimpo22.png" alt="Vasco Araújo" width="100" height="100" style="border-radius: 50%; filter: grayscale(100%); transition: all 0.3s;"></a></h1><br>
This XML Creation API was authored by Flávio Martins nr 99360 and Vasco Araújo nr 98654as part of the Programação Avançada course at ISCTE for the academic year 2023/2024.
