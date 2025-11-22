/*
 * Temporary workaround for [KT-80582](https://youtrack.jetbrains.com/issue/KT-80582)
 * 
 * This file should be safe to be removed once the ticket is closed and the project is updated to Kotlin version which solves that issue.
 */
config.watchOptions = config.watchOptions || {
    ignored: ["**/*.kt", "**/node_modules"]
}

if (config.devServer) {
    config.devServer.static = config.devServer.static.map(file => {
        if (typeof file === "string") {
            return {
                directory: file,
                watch: false,
            }
        } else {
            return file
        }
    })
}