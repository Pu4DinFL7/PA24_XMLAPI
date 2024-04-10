import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestXML {

    @Test
    fun testCreateEntity(){
        val a = XMLEntity("a")
        val b = XMLEntity("b", a)
        assertEquals(b.parent, a)
        assertEquals(a.childrens, listOf(b))


    }
}