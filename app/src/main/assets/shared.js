function findExtendPosition(editor) {
    console.log("findExtendPosition, ");
    const start = editor.selectionStart;
    const end = editor.selectionEnd;
    let string = editor.value;
    let offsetStart = start;
    while (offsetStart > 0) {
        if (!/\s/.test(string[offsetStart - 1]))
            offsetStart--;
        else {
            let os = offsetStart;
            while (os > 0 && /\s/.test(string[os - 1])) {
                os--;
            }
            if ([...string.substring(offsetStart, os).matchAll(/\n/g)].length > 1) {
                break;
            }
            offsetStart = os;
        }
    }
    let offsetEnd = end;
    while (offsetEnd < string.length) {
        if (!/\s/.test(string[offsetEnd + 1]))
            offsetEnd++;
        else {
            let oe = offsetEnd;
            while (oe < string.length && /\s/.test(string[oe + 1])) {
                oe++;
            }
            if ([...string.substring(offsetEnd, oe + 1).matchAll(/\n/g)].length > 1) {
                offsetEnd++;
                break;
            }
            offsetEnd = oe;
        }
    }
    return [offsetStart, offsetEnd];
}
function camel(string) {
    return string.replaceAll(/[ _-]([a-zA-Z])/g, m => m[1].toUpperCase());
}
function snake(string) {
    return string.replaceAll(/(?<=[a-z])[A-Z]/g, m => `_${m}`).toLowerCase()
        .replaceAll(/[ -]([a-z])/g, m => `_${m[1]}`)
}
function upperCamel(string) {
    string = camel(string);
    return string.slice(0, 1).toUpperCase() + string.slice(1);
}
function formatHead(editor, count) {
    // console.log("formatHead, ");
    // let start = editor.selectionStart;
    // const string = editor.value;
    // while (start - 1 > -1 && string.charAt(start - 1) !== '\n') {
    //     start--;
    // }
    // editor.setRangeText('#'.repeat(count || 2) + " ", start, start);

    const start = editor.selectionStart;
    const end = editor.selectionEnd;
    const string = editor.value;


    let offsetStart = start;
    while (offsetStart > 0) {
        if (string[offsetStart - 1] !== '\n')
            offsetStart--;
        else {
            while (offsetStart > 0) {
                if (/\s/.test(string[offsetStart - 1]))
                    offsetStart--;
                else break;
            }
            break;
        }
    }
    let offsetEnd = end;
    while (offsetEnd < string.length) {
        if (string[offsetEnd + 1] !== '\n')
            offsetEnd++;
        else {
            while (offsetEnd < string.length) {
                if (/\s/.test(string[offsetEnd + 1]))
                    offsetEnd++;
                else break;
            }
            break;
        }
    }

    editor.setRangeText(`\n\n${'#'.repeat(count)} ${string.substring(offsetStart, offsetEnd).trim()}\n`, offsetStart,
        offsetEnd);
}
function substringAfterLast(string, delimiter, missingDelimiterValue) {
    const index = string.lastIndexOf(delimiter);
    if (index === -1) {
        return missingDelimiterValue || string;
    } else {
        return string.substring(index + delimiter.length);
    }
}
function substringBeforeLast(string, delimiter, missingDelimiterValue) {
    const index = string.lastIndexOf(delimiter);
    if (index === -1) {
        return missingDelimiterValue || string;
    } else {
        return string.substring(0, index);
    }
}
function substringAfter(string, delimiter, missingDelimiterValue) {
    const index = string.indexOf(delimiter);
    if (index === -1) {
        return missingDelimiterValue || string;
    } else {
        return string.substring(index + delimiter.length);
    }
}
