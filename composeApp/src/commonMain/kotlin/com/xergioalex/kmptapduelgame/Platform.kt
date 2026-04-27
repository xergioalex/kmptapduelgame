package com.xergioalex.kmptapduelgame

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
