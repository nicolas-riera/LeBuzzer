const BUZZ_PATTERN = [125, 45, 25, 45, 20];

let iosSwitch: HTMLLabelElement | null = null;

function iosTick() {
    if (!iosSwitch) {
        const input = document.createElement("input");
        input.type = "checkbox";
        input.setAttribute("switch", "");
        input.id = "haptic-switch";
        iosSwitch = document.createElement("label");
        iosSwitch.htmlFor = input.id;
        iosSwitch.setAttribute("aria-hidden", "true");
        iosSwitch.style.cssText =
            "position:fixed;width:0;height:0;overflow:hidden;opacity:0;pointer-events:none";
        iosSwitch.appendChild(input);
        document.body.appendChild(iosSwitch);
    }
    iosSwitch.click();
}

export function vibrateBuzz() {
    if (typeof navigator.vibrate === "function") {
        navigator.vibrate(BUZZ_PATTERN);
        return;
    }
    let offset = 0;
    BUZZ_PATTERN.forEach((duration, index) => {
        if (index % 2 === 0) {
            if (offset === 0) iosTick();
            else setTimeout(iosTick, offset);
        }
        offset += duration;
    });
}
