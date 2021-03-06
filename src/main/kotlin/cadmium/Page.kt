package cadmium

import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.net.URL
/**
 * Represents a single page opened with Selenium WebDriver
 *
 * Page is one of the core Classes of cadmium.
 * Extend it with your own classes to implement page objects.
 *
 * @property baseURL property that is used to resolve all relative URL
 * @property b Browser instance driving this page
 */
open class Page(private val baseURL: URL, private val b: Browser, private val waiter: Waiter = DefaultWaiterImpl(b)) :
    SearchContext, Waiter by waiter {

    /**
     * Open browser on baseURL
     */
    init {
        b.driver.get(baseURL.toString())
    }

    /**
     * Open given relative URL from current baseURL
     *
     * @param relativeUrl relative URL as seen from current baseURL
     * @param actions executed on Page after opening URL
     * @sample cadmium_test.TestBrowser.testRelativeURL
     */
    fun open(relativeUrl: String, actions: Page.() -> Unit = {}): Page {
        b.driver.get("$baseURL/$relativeUrl")
        actions()
        return this
    }

    /**
     * Get a WebElement and apply given actions on it
     *
     * @param loc Locator used to identify the Element
     * @param actions Extension Function on WebElement which is executed on returned element
     * @return WebElement found by given Locator
     * if multiple elements match the locator, the first is returned
     */
    override fun element(loc: Locator, actions: WebElement.() -> Unit): WebElement {
        val e = WebElement(DriverLocator(b.driver), b.defaultWait, loc)
        e.actions()
        return e
    }

    /**
     * Find all elements within the current page using the given mechanism.
     *
     * @param loc The locating mechanism to use
     * @param waiter optionally controls how long WebDriver is supposed to wait until empty List is returned
     * @return A list of all WebElements, or an empty list if nothing matches
     * @see element
     *
     * At the moment elements is eager and the WebElements returned are not evaluated lazily
     * as claimed in their documentation.
     * Todo: return a lazily evaluated range of WebElements instead
     */
    override fun elements(loc: Locator, waiter: WebDriverWait): List<WebElement> {
        return b.driver.findElements(loc.by).map { WebElement(DriverLocator(b.driver), waiter, loc) }
    }

    /**
     * overload of elements which uses defaultWait
     */
    override fun elements(loc: Locator): List<WebElement> = elements(loc, b.defaultWait)

    /**
     * Shorthand for click on element given by locator ( element(loc).click() )
     */
    fun click(loc: Locator) = element(loc).click()

    /**
     * Shorthand for clicking on input element with given test as value
     */
    fun click(text: String) = element(XPath("//input[@value=\"$text\"]")).click()

    /**
     * Waits for an Alert dialog to appear
     *
     * @return handle to present Alert
     * @Todo convert this to a proper Waiter condition which still returns a cadmium.Alert
     */
    fun waitForAlert(): Alert {
        return Alert(waitUntil(ExpectedConditions.alertIsPresent()))
    }

    /**
     * The source of the current page
     *
     * If the page has been modified after loading (for
     * example, by Javascript) there is no guarantee that the returned text is that of the modified
     * page.
     *
     * @see org.openqa.selenium.WebDriver.getPageSource
     */
    val source: String
        get() = b.driver.pageSource

    /**
     * The title of the current page, with leading and trailing whitespace stripped, or null
     * if no title is set yet.
     */
    val title: String?
        get() = b.driver.title

    /**
     * Waiter condition which returns true when a pageload was triggered
     *
     * Idea is taken from:
     * <https://blog.codeship.com/get-selenium-to-wait-for-page-load/>
     */
    val pageLoad =
        ExpectedConditions.stalenessOf(b.driver.findElement(By.tagName("html")))!!
}

/**
 * Apply given actions on page.
 *
 * Visit both as in visiting a page and the Visitor pattern
 *
 * @param actions applied to page object
 */
fun <T : Page> T.visit(actions: T.() -> Unit) {
    actions()
}
