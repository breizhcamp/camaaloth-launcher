Vue.directive('pad-click', {

    // When the bound element is inserted into the DOM...
    bind: function (el, binding) {
        ws.sub("/pad", msg => {
            let body = JSON.parse(msg.body)

            let button = binding.value.button ? binding.value.button : binding.value
            let bank = binding.value.bank
            let disable = (typeof binding.value.disable === "boolean" ? binding.value.disable : true)

            if (body.action === "PRESSED" && button == body.button && (!bank || bank === body.bank) && !el.classList.contains('disabled')) {
                el.click()

                if (disable) {
                    el.classList.add('disabled')
                    el.disabled = true
                }
            }
        })
    }
})