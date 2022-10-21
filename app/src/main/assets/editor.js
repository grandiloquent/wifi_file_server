let baseUri = (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') ? '' : '';
const textarea = document.querySelector('textarea');
const customLayout = document.querySelector('custom-layout');
const bottomBar = document.querySelector('.bottom-bar');
let translate = 'https://kingpunch.cn';
let language = new URL(window.location).searchParams.get('language');
const searchParams = new URL(window.location.href).searchParams;
const id = searchParams.get('id');

textarea.addEventListener('keydown', function (e) {
    if (e.keyCode === 9) {
        const p = findExtendPosition(textarea);
        textarea.setRangeText(
            textarea.value.substring(p[0], p[1])
                .split('\n')
                .map(i => {
                    return '\t' + i;
                })
                .join('\n'), p[0], p[1]);
        this.selectionStart = this.selectionEnd = start + 1;
        // prevent the focus lose
        e.preventDefault();
    }
}, false);
document.addEventListener('visibilitychange', async ev => {
    localStorage.setItem('content', textarea.value);

});

function createToolItem(d, title, callback) {
    const template = `<div  class="bottom-bar-item" style="height:100%">
        <div class="bottom-bar-item-wrapper">
            <div class="c3-icon">
                <svg viewBox="0 0 24 24">
                    <path d="${d}"></path>
                </svg>
            </div>
            <div class="bottom-bar-item-text">
                ${title}
            </div>
        </div>
    </div>`;
    const div = document.createElement('div');
    div.innerHTML = template;
    bottomBar.appendChild(div);
    div.addEventListener('click', callback)
}


createToolItem("M5.016 3.984h13.969v3h-5.484v12h-3v-12h-5.484v-3z", "标题", () => {
    formatHead(textarea, 2);

})

createToolItem('M14.578 16.594l4.641-4.594-4.641-4.594 1.406-1.406 6 6-6 6zM9.422 16.594l-1.406 1.406-6-6 6-6 1.406 1.406-4.641 4.594z', '分行', () => {
    //formatCode(textarea)
    textarea.value = textarea.value.split('.').map(x => x.trim() + ".").join('\n\n');
})
createToolItem('M14.578 16.594l4.641-4.594-4.641-4.594 1.406-1.406 6 6-6 6zM9.422 16.594l-1.406 1.406-6-6 6-6 1.406 1.406-4.641 4.594z', '代码块', async () => {
    //     const p = findExtendPosition(textarea);
    //     console.log(p, textarea.value.substring(p[0], p[1]))
    //     textarea.setRangeText(`\`\`\`${language || 'pgsql'}
    // ${textarea.value.substring(p[0], p[1])}
    // \`\`\``, p[0], p[1]);
    const uri = await navigator.clipboard.readText();
    if (uri.startsWith("http://") || uri.startsWith("https://"))
        await uploadUri(uri, textarea)
    else
        uploadHanlder(textarea);
})



createToolItem('M21.516 20.484v-13.969q0-0.422-0.305-0.727t-0.727-0.305h-9.047l1.313 3.797h1.453v-1.266h1.266v1.266h4.547v1.313h-1.922q-0.703 2.344-2.391 4.219l3.281 3.281-0.938 0.891-3.094-3.094 1.031 3.094-1.969 2.531h6.469q0.422 0 0.727-0.305t0.305-0.727zM13.172 10.594l0.797 2.344 0.844 1.125q1.453-1.594 2.063-3.469h-3.703zM6.984 15.984q2.156 0 3.492-1.359t1.336-3.516q0-0.047-0.141-1.031h-4.688v1.734h2.953q-0.094 0.891-0.844 1.641t-2.109 0.75q-1.313 0-2.227-0.938t-0.914-2.25q0-1.359 0.914-2.297t2.227-0.938q1.266 0 2.063 0.797l1.313-1.266q-1.453-1.313-3.375-1.313-2.063 0-3.516 1.477t-1.453 3.539 1.453 3.516 3.516 1.453zM21 3.984q0.797 0 1.406 0.609t0.609 1.406v15q0 0.797-0.609 1.406t-1.406 0.609h-9l-0.984-3h-8.016q-0.797 0-1.406-0.609t-0.609-1.406v-15q0-0.797 0.609-1.406t1.406-0.609h6.984l1.031 3h9.984z', '翻译', async () => {
    await trans(textarea, 1);
})
createToolItem('M18.984 6.422l-5.578 5.578 5.578 5.578-1.406 1.406-5.578-5.578-5.578 5.578-1.406-1.406 5.578-5.578-5.578-5.578 1.406-1.406 5.578 5.578 5.578-5.578z', '删除', async () => {

    removeLines();
})
createToolItem('M21.516 20.484v-13.969q0-0.422-0.305-0.727t-0.727-0.305h-9.047l1.313 3.797h1.453v-1.266h1.266v1.266h4.547v1.313h-1.922q-0.703 2.344-2.391 4.219l3.281 3.281-0.938 0.891-3.094-3.094 1.031 3.094-1.969 2.531h6.469q0.422 0 0.727-0.305t0.305-0.727zM13.172 10.594l0.797 2.344 0.844 1.125q1.453-1.594 2.063-3.469h-3.703zM6.984 15.984q2.156 0 3.492-1.359t1.336-3.516q0-0.047-0.141-1.031h-4.688v1.734h2.953q-0.094 0.891-0.844 1.641t-2.109 0.75q-1.313 0-2.227-0.938t-0.914-2.25q0-1.359 0.914-2.297t2.227-0.938q1.266 0 2.063 0.797l1.313-1.266q-1.453-1.313-3.375-1.313-2.063 0-3.516 1.477t-1.453 3.539 1.453 3.516 3.516 1.453zM21 3.984q0.797 0 1.406 0.609t0.609 1.406v15q0 0.797-0.609 1.406t-1.406 0.609h-9l-0.984-3h-8.016q-0.797 0-1.406-0.609t-0.609-1.406v-15q0-0.797 0.609-1.406t1.406-0.609h6.984l1.031 3h9.984z', '翻译', async () => {
    await trans(textarea, 0);
})

if (id)
    render();


async function submitData() {
    const firstLine = textarea.value.trim().split("\n", 2)[0];
    const obj = {

        content: substringAfter(textarea.value.trim(), "\n"),
        title: firstLine.split('|')[0].replace(/^#+ +/, ''),
    };
    if (id) {
        obj.id = parseInt(id);
    }
    const response = await fetch(`${baseUri}/api/note`, {
        method: 'POST',
        body: JSON.stringify(obj)
    });
    return await response.text();
}

async function loadData() {

    const response = await fetch(`${baseUri}/api/note?id=${id}`);
    return await response.json();
}

async function render() {
    textarea.value = localStorage.getItem("content");
    try {
        const obj = await loadData();
        document.title = obj.title;
        textarea.value = `# ${obj.title}
    
${obj.content.trim()}
    `
    } catch (error) {
        console.log(error)
    }
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
            /* while (offsetEnd < string.length) {
                 if (/\s/.test(string[offsetEnd + 1]))
                     offsetEnd++;
                 else break;
             }*/
            offsetEnd++;
            break;
        }
    }
    const str = string.substring(offsetStart, offsetEnd).trim();
    if (str.startsWith('#')) {
        editor.setRangeText(`\n\n#${str}\n`, offsetStart,
            offsetEnd);
    } else {
        editor.setRangeText(`\n\n${'#'.repeat(count)} ${str}\n`, offsetStart,
            offsetEnd);
    }
    editor.selectionStart = offsetStart + 1;
}

function formatIndent(editor) {
    let start = editor.selectionStart;
    let end = editor.selectionEnd;
    let string = editor.value;
    while (start - 1 > -1) {
        if (string.codePointAt(start - 1) === 10) {
            let index = start - 1;
            let found = false;
            while (index - 1 > -1 && /\s/.test(string[index - 1])) {
                if (string.codePointAt(index - 1) === 10) {
                    found = true;
                    break;
                }
                index--;
            }
            start = index;
            if (found) {
                break;
            }
        }
        start--;
    }
    // && string.codePointAt(++end) !== 10
    while (end + 1 < string.length) {
        if (string.codePointAt(end + 1) === 10) {
            let index = end + 1;
            let found = false;
            while (end + 1 < string.length && /\s/.test(string[index + 1])) {
                if (string.codePointAt(index + 1) === 10) {
                    found = true;
                    break;
                }
                index++;
            }
            end = index;
            if (found) {
                break;
            }
        }
        end++;
    }
    let lines = `${string.substring(start, end + 1).trim()}`
        .split('\n')
        .map(i => {
            if (/^ {4}\S+/.test(i)) {
                return `${i.substring(4)}`;
            }
            if (/^- \S+/.test(i)) {
                return `${' '.repeat(4)}${i.substring(2)}`;
            }
            if (/^\d+\. \S+/.test(i)) {
                return `${' '.repeat(4)}${i.substring(
                    /^(\d+\. )\S+/.exec(i).length + 1
                )}`;
            }
            return `${' '.repeat(4)}${i}`;
        })
    const str = '\n' + lines.join('\n') + '\n';


    editor.setRangeText(str,
        Math.max(start, 0), end + 1
    )
    editor.selectionStart = start + 5;
    editor.selectionEnd = start + 5;
    editor.focus();
}

function formatList(textarea) {
    const p = findExtendPosition(textarea);
    textarea.setRangeText(
        textarea.value.substring(p[0], p[1])
            .split('\n')
            .map(i => {
                // console.log(i);
                // if (i.startsWith('*')) {
                //     return i.substring(2);
                // } else {}
                return '- ' + i;
            })
            .join('\n'), p[0], p[1]);
}

async function formatCode(editor) {
    const start = editor.selectionStart;
    const end = editor.selectionEnd;
    const string = editor.value;
    if (start === end) {
        let offsetStart = start;
        let offsetEnd = end;
        /*while (offsetEnd < string.length) {
            if (/[a-zA-Z0-9+_.\(\)<>:-]/.test(string[offsetEnd + 1]))
                offsetEnd++;
            else break;
        }*/
        //  !/\s/.test(string.charAt(offsetStart - 1)) &&
        // /\s/.test(string.charAt(offsetEnd + 1)) || localStorage.getItem('formatPattern')
        const regex = new RegExp((localStorage.getItem('formatPattern') || '[a-zA-Z0-9*+?|{\\[()^$._#=\\]!]'));

        while (offsetStart > 0 &&
            regex.test(string.charAt(offsetStart - 1))) {
            /*&& string.charAt(offsetStart - 1) !== '\n'
            && string.charAt(offsetStart - 1) !== '/'
            && string.charAt(offsetStart - 1) <= 'z') {*/
            offsetStart--;
        }
        if (offsetEnd < string.length && !/\s/.test(string.charAt(offsetEnd))) {
            while (offsetEnd + 1 < string.length) {
                if (!regex.test(string.charAt(offsetEnd + 1))) {
                    //                    if (string.charAt(offsetEnd + 1) > 'z' || string.charAt(offsetEnd + 1) === '\n' || string.charAt(offsetEnd + 1) === '/') {
                    // good good good
                    offsetEnd++;
                    break;
                }
                offsetEnd++;
            }
        }
        const str = string.substring(offsetStart, offsetEnd).trim();
        if (str) {
            if ([...str.matchAll(/`/g)].length > 1) {
                editor.setRangeText(' ' + str.replaceAll('`', '') + ' ', offsetStart,
                    offsetEnd);
            } else {
                /*editor.setRangeText(' `' + str + '` ', offsetStart,
                    offsetEnd);*/
                editor.setRangeText('`' + str + '`', offsetStart,
                    offsetEnd);
            }
        } else {
            let strings;
            if (typeof NativeAndroid !== 'undefined')
                strings = NativeAndroid.readText();
            else {
                try {
                    strings = await navigator.clipboard.readText();
                } catch (e) {

                }
            }
            const contents = "```" + (language || 'pgsql') + "\n" + (strings) + "\n```\n\n";
            editor.setRangeText(contents, offsetStart,
                offsetEnd);
            editor.selectionStart = offsetStart + contents.length;
        }
    } else {
        const str = string.substring(start,
            end).trim();
        // ==, !=, >, >=, <, and <=.
        editor.setRangeText(str.split(/, (and )*/g).filter(x => x).map(x => `\`${x}\`、`).join('').replace("、`and `、", " 和 "), start,
            end);
    }
}

function findExtendPosition(editor) {
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
        if (!/\s/.test(string[offsetEnd + 1])) {

            offsetEnd++;
        } else {

            let oe = offsetEnd;
            while (oe < string.length && /\s/.test(string[oe + 1])) {
                oe++;
            }
            if ([...string.substring(offsetEnd, oe + 1).matchAll(/\n/g)].length > 1) {
                offsetEnd++;

                break;
            }
            offsetEnd = oe + 1;

        }
    }
    while (offsetStart > 0 && string[offsetStart - 1] !== '\n') {
        offsetStart--;
    }
    // if (/\s/.test(string[offsetEnd])) {
    //     offsetEnd--;
    // }
    return [offsetStart, offsetEnd];
}

async function trans(editor, english) {
    // let start = editor.selectionStart;
    // let end = editor.selectionEnd;
    // const string = editor.value;
    // while (start > 0 && string[start - 1] !== '\n') {
    //     start--;
    // }
    // while (end < string.length) {
    //     end++;
    //     if (string[end] === '\n') break;
    // }
    // const value = string.substring(start, end);
    // if (!value.trim()) return;
    // const lines = await google(value, english);
    // editor.setRangeText(`${lines[0].join(' ')}\n\n${lines[1].join(' ')}`, start, end);
    const points = findExtendPosition(editor);
    const string = editor.value.substring(points[0], points[1]);

    const value = string.replaceAll(/\n/g, ' ');
    if (!value.trim()) return;
    const lines = await google(value, english);
    let results = lines[1].join(' ');
    const pattern = localStorage.getItem('string');
    if (pattern && pattern.trim().length) {
        const values = pattern.split('\n').filter(i => i.trim().length).map(i => i.trim());
        console.log(values);
        for (let i = 0; i < values.length; i += 2) {
            if (i + 1 < values.length) {
                results = results.replaceAll(values[i], values[i + 1]);
            }
        }
    }
    let year = '';
    let matchYear = /\d{4}/.exec(value);
    if (matchYear) {
        year = matchYear[0] + '年'
    }
    editor.setRangeText(`${english ? string : (lines[0].join(' '))}\n\n${results}\n\n${year}`, points[0], points[1]);
}

async function google(value, english) {
    // https://service-mayeka3y-1258705152.hk.apigw.tencentcs.com/release/
    // https://service-ehkp0lyi-1301282710.hk.apigw.tencentcs.com/release/
    const response = await fetch(`${translate}/translate?q=${encodeURIComponent(value.trim())}&to=${english ? "zh" : "en"}`);
    const obj = await response.text();
    const lines1 = [];
    const lines2 = [];
    const translated = JSON.parse(obj.replaceAll(/您/g, '你').replaceAll(/ - /g, "——"));
    if (translated.sentences) {
        const sentences = translated.sentences;
        for (let index = 0; index < sentences.length; index++) {
            const element = sentences[index];
            lines1.push(element.orig);
            lines2.push(element.trans);
        }
    } else {
        const trans = translated.trans_result;
        for (let index = 0; index < trans.length; index++) {
            const element = trans[index];
            lines1.push(element.src);
            lines2.push(element.dst);
        }
    }
    return [lines1, lines2];
}


textarea.value = localStorage.getItem('content');


document.addEventListener('keydown', async ev => {
    if (ev.key === 'F1') {
        ev.preventDefault();
        textarea.setRangeText(
            `\`${textarea.value.substring(textarea.selectionStart, textarea.selectionEnd)}\``
            , textarea.selectionStart, textarea.selectionEnd)
    } else if (ev.key === 'F3') {
        ev.preventDefault();
        await trans(textarea, 1);
    } else if (ev.key === 'F5') {
        ev.preventDefault();
        saveData();
    } else if (ev.key === 'F10') {
        ev.preventDefault();
        //removeLines();
        uploadHanlder(textarea);
    } else {
        if (ev.altKey && ev.key.toLowerCase() === 's') {
            ev.preventDefault();
            saveData();
        } else if (ev.ctrlKey && ev.key.toLowerCase() === 's') {
            ev.preventDefault();
            saveData();
        } else if (ev.ctrlKey && ev.key.toLowerCase() === 'd') {
            ev.preventDefault();
            copyLine(textarea)
        } else if (ev.ctrlKey && ev.key.toLowerCase() === 'q') {
            ev.preventDefault();
            setText(textarea, `## ${(await navigator.clipboard.readText())}`)
        } else if (ev.ctrlKey && ev.key.toLowerCase() === 'g') {
            ev.preventDefault();
            setText(textarea, `${(await navigator.clipboard.readText()).replaceAll(/[\n\r]/g, '')}`)
        } else if (ev.ctrlKey && ev.key.toLowerCase() === 'h') {
            ev.preventDefault();
            formatHead(textarea, 2);
        } else if (ev.ctrlKey && ev.key.toLowerCase() === 'o') {
            ev.preventDefault();
            textarea.value = parse(textarea.value).split('.').map(x => x.trim() + '.\n').join('\n')
        } else if (ev.ctrlKey && ev.key.toLowerCase() === 'p') {
            ev.preventDefault();
            window.open(`article.html?id=${id}`, '_blank')
        }
    }
});

function parse(text) {

    let lines = text.split("\n");
    let transcript = [];
    for (let i = 0; i < lines.length; i++) {
        let line = lines[i];

        if (/^\d{1,}$/.test(line.trim()) || line.indexOf(" --> ") !== -1 || /[\u4e00-\u9fa5]/.test(line)) {
            continue;
        }
        transcript.push(line);
    }

    return transcript.join(' ');
}

async function saveData() {
    const res = await submitData();
    if (id)
        document.getElementById('toast').setAttribute('message', '成功');
    else
        window.location = `${window.location.origin}${window.location.pathname}?id=${res}`

}

async function removeLines() {
    if (textarea.selectionStart === textarea.selectionEnd) {
        const p = findExtendPosition(textarea);

        let start = p[0];

        while (start > -1 && /\s/.test(textarea.value[start - 1])) {
            start--;
        }

        let end = p[1];
        while (end + 1 < textarea.value.length && /\s/.test(textarea.value[end + 1])) end++;

        if (typeof NativeAndroid !== 'undefined') {
            NativeAndroid.writeText(textarea.value.substring(start, end));
        } else {
            await navigator.clipboard.writeText(textarea.value.substring(start, end))
        }
        textarea.setRangeText('\n', start, end);
        textarea.selectionEnd = start;
    } else {
        textarea.value = textarea.value.substring(textarea.selectionEnd);
        textarea.selectionStart = 0;
        textarea.selectionEnd = 0;
        textarea.scrollLeft = 0;
        textarea.scrollTop = 0;
    }

}

function setText(editor, string) {
    editor.setRangeText(string, editor.selectionStart, editor.selectionEnd);
    editor.selectionStart = editor.selectionStart + string.length;
}

function insertBeginLine(editor, string) {
    let s = editor.selectionStart;
    while (s - 1 > -1 && string[s - 1] !== '\n') s--;
    editor.setRangeText(string, s, s);
}

async function uploadImage(image, name) {
    const form = new FormData();
    form.append('images', image, name)
    const response = await fetch(`https://lucidu.cn/api/article/2`, {
        method: 'POST',
        body: form
    });
    return await response.text();
}

function uploadHanlder(editor) {
    tryUploadImageFromClipboard((ok) => {
        const string = `![](https://static.lucidu.cn/images/${ok})\n\n`;
        editor.setRangeText(string, editor.selectionStart, editor.selectionStart);
    }, () => {
        const input = document.createElement('input');
        input.type = 'file';
        input.addEventListener('change', async ev => {
            const file = input.files[0];
            const imageFile = await uploadImage(file, file.name);
            const string = `![](https://static.lucidu.cn/images/${imageFile})\n\n`;
            editor.setRangeText(string, editor.selectionStart, editor.selectionStart);
        });
        input.click();
    });
}

function tryUploadImageFromClipboard(success, error) {
    navigator.permissions.query({
        name: "clipboard-read"
    }).then(result => {
        if (result.state === "granted" || result.state === "prompt") {
            navigator.clipboard.read().then(data => {
                console.log(data[0].types);
                const blob = data[0].getType("image/png");
                console.log(blob.then(res => {
                    const formData = new FormData();
                    formData.append("images", res, "1.png");
                    fetch(`https://lucidu.cn/api/article/2`, {
                        method: "POST",
                        body: formData
                    }).then(res => {
                        return res.text();
                    }).then(obj => {
                        success(obj);
                    })
                }).catch(err => {
                    console.log(err)
                    error(err);
                }))
            })
                .catch(err => {
                    error(err);
                });
        } else {
            error(new Error());
        }
    });
}

function writeText(message) {
    const textarea = document.createElement("textarea");
    textarea.style.position = 'fixed';
    textarea.style.right = '100%';
    document.body.appendChild(textarea);
    textarea.value = message;
    textarea.select();
    document.execCommand('copy');
    textarea.remove();
}

function readText() {
    const textarea = document.createElement("textarea");
    textarea.style.position = 'fixed';
    textarea.style.right = '100%';
    document.body.appendChild(textarea);
    textarea.value = message;
    textarea.select();
    document.execCommand('paste');
    return textarea.value;
}

function copyLine(editor, count) {
    const start = editor.selectionStart;
    const end = editor.selectionEnd;
    const string = editor.value;
    let offsetStart = start;
    while (offsetStart > 0) {
        if (string[offsetStart - 1] !== '\n')
            offsetStart--;
        else {
            // while (offsetStart > 0) {
            //     if (/\s/.test(string[offsetStart - 1]))
            //         offsetStart--;
            //     else break;
            // }
            break;
        }
    }
    let offsetEnd = end;
    while (offsetEnd < string.length) {
        if (string[offsetEnd + 1] !== '\n')
            offsetEnd++;
        else {
            /* while (offsetEnd < string.length) {
                 if (/\s/.test(string[offsetEnd + 1]))
                     offsetEnd++;
                 else break;
             }*/
            offsetEnd++;
            break;
        }
    }
    const str = string.substring(offsetStart, offsetEnd).trim();
    writeText(str);
    editor.focus()
}

async function uploadUri(uri, editor) {
    const res = await fetch(uri);
    const data = await res.blob();
    const form = new FormData();
    form.append('images', data, substringBeforeLast(substringAfterLast(uri, "/"), '?'))
    const response = await fetch(`https://lucidu.cn/api/article/2`, {
        method: 'POST',
        body: form
    });
    const imageFile = await response.text();
    const string = `![](https://static.lucidu.cn/images/${imageFile})\n\n`;
    editor.setRangeText(string, editor.selectionStart, editor.selectionStart);
}
