package content

import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLUListElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.xhr.XMLHttpRequest
import org.w3c.dom.XMLDocument as document
import kotlin.browser.*

external object chrome {
    object MessageSender {
        var tab: tabs.Tab
        var frameId: Int
        var id: String
        var url: String
        var tlsChannelId: String
    }

    object extension {
        fun getURL(queryInfo: String): String = definedExternally
    }

    object runtime {
        fun sendMessage(data: Any?, callback: (() -> Unit)?): Unit = definedExternally
        fun connect(data: Any?): Port = definedExternally
        object tabs {
            fun connect(data: Any?): Port = definedExternally
        }
        object Port {
            var name: String
            var disconnect: (() -> Unit)?
            var onDisconnect: Any
            var onMessage: onMessage
            fun postMessage(data: Any?): Unit = definedExternally
            fun postMessage(data: chrome.tabs.Tab): Unit = definedExternally

        }
        object onConnect {
            fun addListener(callback: (port: Port) -> Unit): Unit = definedExternally
        }
        object onMessage {
            fun addListener(callback: ((request: dynamic, sender: MessageSender, sendResponse: (() -> Unit)?) -> Unit)?): Unit = definedExternally
            fun addListener(callback: (msg: dynamic) -> Unit): Unit = definedExternally

        }
    }

    object tabs {
        object Tab {
            var status: String?
            var index: Number
            var openerTabId: Number?
            var title: String?
            var url: String?
            var pinned: Boolean
            var highlighted: Boolean
            var windowId: Number
            var active: Boolean
            var favIconUrl: String?
            var id: Number
            var incognito: Boolean
        }
        fun getAllInWindow(queryInfo: Any?, callback: (result: Array<Tab>) -> Unit): Unit = definedExternally
        fun update(tabId: Number, updateProperties: Any, callback: (() -> Unit)?): Unit = definedExternally
    }
}


fun main(args: Array<String>) {
    document.addEventListener("keyup", fun(e: Event) {
        if ((e as KeyboardEvent).ctrlKey && e.shiftKey && e.keyCode == 88) {
            showOverlay()
        }
        if (e.keyCode == 27) {
            hideOverlay()
        }
    }, null)
}

fun showOverlay() {
    val port = chrome.runtime.connect(js("({name: 'tabListener'})"))
    document.body?.load(chrome.extension.getURL("out/production/content/content.html"))
    port.postMessage(js("({task: 'getTabs'})"))
    port.onMessage.addListener(fun(msg) {
        addTabToList(msg)
    })
}

fun addTabToList(text: String) {
    val tabList = document.getElementById("tab_list")
    val tabEntry: HTMLUListElement = document.createElement("ul") as HTMLUListElement
    tabEntry.textContent = text
    tabList?.appendChild(tabEntry)
}

fun hideOverlay() {
    document.getElementById("overlay")?.remove()
}

fun HTMLElement.load(url: String) {
    val request = XMLHttpRequest()
    request.open("GET", url, true)
    request.onreadystatechange = {
        if (!(request.readyState != 4.toShort() || request.status != 200.toShort())) {
            this.innerHTML += request.responseText
        }
    }
    request.send()
}