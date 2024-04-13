import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestXML {

    @Test
    fun testCreateEntity(){
        val a = XMLEntity("a")
        val b = XMLEntity("b", a)
        val c = XMLEntity("c", a)
        val d = XMLEntity("d", b)
        val e = XMLEntity("e", d)
       // assertEquals(b.parent, a)
        //assertEquals(a.childrens, listOf(b))
        val doc = XMLDocument(a)
        //assertEquals(doc.addEntity("e",d.name),e)
        assertEquals(doc.removeEntity("e"),d)





    }
}