import org.openqa.selenium.By

abstract class Locator {
    abstract val by : By
}

// We assert non-null here as the java selenium by functions never return null
class Name(v: String) : Locator() { override val by = By.name(v)!! }
class Id(v: String) : Locator() { override val by = By.id(v)!! }
class Label(v: String) : Locator() { override val by = By.xpath("//input[@id=string(//label[contains(text(),\"$v\")]/@for)]")!! }
class XPath(v: String) : Locator() { override val by = By.xpath(v)!! }
class ClassName(v: String) : Locator() {override val by = By.className(v)!!}