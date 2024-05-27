
<h1 align="center">
  XML Creation API <br>
  Programação Avançada @ ISCTE 2023/2024
</h1>

## Introduction

The XML Creation API is a Kotlin library designed to facilitate the creation, manipulation, and serialization of XML documents. It provides a set of classes and functions that allow developers to build XML documents programmatically, define XML entities, attributes, and relationships, and export the resulting XML to files.

Features

  Simple and Intuitive: Easily create XML documents using Kotlin syntax.
  
  Validation: Validate XML version, encoding, and string formats to ensure compliance.
  
  Visitor Pattern: Utilize the Visitor Pattern design for traversing and manipulating XML entities.
  
  Visualization: You can visualize the XML document you are creating through the to Tree method.
  
  Querying: Perform XPath-like queries to retrieve specific entities from the document.

  Programmatic XML Document Creation: Developers can create XML documents programmatically using Kotlin DSL provided by the API.

  Annotation-Based XML Entity Construction: The API supports the creation of XML entities from Kotlin classes using reflection and annotations. This feature enables seamless translation of Kotlin objects into XML entities.

  Flexible XML Entity Manipulation: Users can manipulate XML entities by adding, removing, or editing attributes and child entities using lambda functions to specify acceptance criteria.

  XML Serialization: The API allows serialization of XML documents to files in XML format, enabling storage and transmission of XML data.

## XML Entity
The XMLEntity class represents an XML entity with a name, parent, attributes, and plain text content. It supports attribute manipulation, child entity creation, and visitor pattern acceptance.
Creating one can be done through one of two constructors.

Simple Constructor<br> 
This constructor takes a string as a parameter to name the entity. Optionally, you can specify a parent XML entity. This method provides a straightforward approach to entity creation.
```
    val europe = XMLEntity("Europe")
    val portugal = XMLEntity("Portugal", europe)

    //This code will make an XMLEntity "Portugal" which is a child of the XMLEntity "Europe"
```

Secondary Constructor<br>
This constructor accepts any object from a Kotlin class. It then translates that object to an XML entity using Reflection and Annotation. This method offers flexibility and convenience, especially when dealing with complex data structures.

```
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
