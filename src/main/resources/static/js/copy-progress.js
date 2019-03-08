/** subscribe to copy updates */
copyProgress = function () {
    let callbacks = []
    let lastMsg

    ws.sub("/050-copy-progress", msg => {
        let lastRemaining = lastMsg ? lastMsg.remaining : 0

        lastMsg = JSON.parse(msg.body)

        //adding some computed properties
        if (lastMsg.current) {
            if (lastMsg.speed) {
                lastMsg.remaining = Math.round((lastMsg.current.size + lastMsg.waitingSize - lastMsg.copied) / lastMsg.speed)
            } else {
                lastMsg.remaining = lastRemaining
            }
        }

        callbacks.forEach(c => c(lastMsg))
    })

    return function(callback) {
        callbacks.push(callback)
        if (lastMsg) {
            callback(lastMsg)
        }
    }
}()