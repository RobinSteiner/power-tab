package main

import org.w3c.dom.XMLDocument as document
import org.w3c.dom.events.*
import kotlin.browser.*
import kotlin.dom.appendElement
import kotlin.dom.appendText

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
        fun connect(tabId: Number, data: Any?): runtime.Port = definedExternally
        fun getAllInWindow(queryInfo: Any?, callback: (result: Array<Tab>) -> Unit): Unit = definedExternally
        fun update(tabId: Number, updateProperties: Any, callback: (() -> Unit)?): Unit = definedExternally
        fun getSelected(queryInfo: Any?, callback: (result: Tab) -> Unit): Unit = definedExternally
    }
}

fun renderStatus(statusText: String?) {
    if(statusText != null) {
        val status = document.getElementById("status")
        if(status != null) {
            status.appendText(statusText)
            status.appendElement("br", {})
        }
    }
}

fun main(args: Array<String>) {
    console.log("Kotlin extension popup script running!")
    var port: chrome.runtime.Port

    chrome.tabs.getSelected(null, fun(tab: chrome.tabs.Tab) {
        port = chrome.tabs.connect(tab.id, js("({name: 'tabListener'})"))
    })

    chrome.runtime.onConnect.addListener(fun(port) {
        port.onMessage.addListener(fun(msg) {
            if(msg.task == "getTabs") {
                chrome.tabs.getAllInWindow(null) { tabs ->
                    if (tabs.isNotEmpty()) {
                        tabs.forEach {
                            port.postMessage(it)
                        }
                        //chrome.tabs.update(tabs[0].id, js("({active: true})") , null)
                    }
                }
            }
        })
    })

}