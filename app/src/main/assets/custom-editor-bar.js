class CustomEditorBar extends HTMLElement {

    constructor() {
        super();

        this.root = this.attachShadow({mode: 'open'});
        this.root.innerHTML = `<style>.text
{
    max-width: 100%;
    padding: 0 4px;
    box-sizing: border-box;
    display: block;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    color: #030303;
}
.image
{
    flex-shrink: 0;
    width: 24px;
    height: 24px;
    fill: currentColor;
    stroke: none;
    color: #030303;
    display: block;
}
.item
{
    display: flex;
    -webkit-box-flex: 1;
    flex: 1 1 0%;
    -webkit-box-orient: vertical;
    -webkit-box-direction: normal;
    flex-direction: column;
    -webkit-box-align: center;
    align-items: center;
    -webkit-box-pack: center;
    justify-content: center;
    overflow: hidden;
    color: #030303;
}</style>
    <div style="display: flex; justify-content: space-around; position: fixed; bottom: 0; left: 0; right: 0; padding: 0 env(safe-area-inset-right) env(safe-area-inset-bottom) env(safe-area-inset-left); z-index: 3; height: 48px; border-top: 1px solid rgba(0,0,0,.1); background: rgba(255,255,255,.98); color: #030303; font-size: 1.1rem;">
      <div style="display: flex; -webkit-box-flex: 1; flex: 1 1 0%; min-width: 0;">
        <div class="item" id="head">
          <div class="image">
            <svg xmlns="http://www.w3.org/2000/svg" enable-background="new 0 0 24 24" height="24" viewBox="0 0 24 24" width="24">
              <g>
                <path d="M5.016 3.984h13.969v3h-5.484v12h-3v-12h-5.484v-3z">
                </path>
              </g>
            </svg>
          </div>
          <div class="text">
            标题
          </div>
        </div>
        <div class="item" id="english">
          <div class="image">
            <svg xmlns="http://www.w3.org/2000/svg" height="24" viewBox="0 0 24 24" width="24">
              <path d="M21.516 20.484v-13.969q0-0.422-0.305-0.727t-0.727-0.305h-9.047l1.313 3.797h1.453v-1.266h1.266v1.266h4.547v1.313h-1.922q-0.703 2.344-2.391 4.219l3.281 3.281-0.938 0.891-3.094-3.094 1.031 3.094-1.969 2.531h6.469q0.422 0 0.727-0.305t0.305-0.727zM13.172 10.594l0.797 2.344 0.844 1.125q1.453-1.594 2.063-3.469h-3.703zM6.984 15.984q2.156 0 3.492-1.359t1.336-3.516q0-0.047-0.141-1.031h-4.688v1.734h2.953q-0.094 0.891-0.844 1.641t-2.109 0.75q-1.313 0-2.227-0.938t-0.914-2.25q0-1.359 0.914-2.297t2.227-0.938q1.266 0 2.063 0.797l1.313-1.266q-1.453-1.313-3.375-1.313-2.063 0-3.516 1.477t-1.453 3.539 1.453 3.516 3.516 1.453zM21 3.984q0.797 0 1.406 0.609t0.609 1.406v15q0 0.797-0.609 1.406t-1.406 0.609h-9l-0.984-3h-8.016q-0.797 0-1.406-0.609t-0.609-1.406v-15q0-0.797 0.609-1.406t1.406-0.609h6.984l1.031 3h9.984z" />
            </svg>
          </div>
          <div class="text">
            翻译
          </div>
        </div>
        <div class="item" id="save">
          <div class="image">
            <svg xmlns="http://www.w3.org/2000/svg" height="24" viewBox="0 0 24 24" width="24">
              <path d="M15 9v-3.984h-9.984v3.984h9.984zM12 18.984q1.219 0 2.109-0.891t0.891-2.109-0.891-2.109-2.109-0.891-2.109 0.891-0.891 2.109 0.891 2.109 2.109 0.891zM17.016 3l3.984 3.984v12q0 0.797-0.609 1.406t-1.406 0.609h-13.969q-0.844 0-1.43-0.586t-0.586-1.43v-13.969q0-0.844 0.586-1.43t1.43-0.586h12z" />
            </svg>
          </div>
          <div class="text">
            保存
          </div>
        </div>
        <div class="item" id="menu">
          <div class="image">
            <svg xmlns="http://www.w3.org/2000/svg" height="24" viewBox="0 0 24 24" width="24">
              <path d="M3 6h18v2.016h-18v-2.016zM3 12.984v-1.969h18v1.969h-18zM3 18v-2.016h18v2.016h-18z">
              </path>
            </svg>
          </div>
          <div class="text">
            菜单
          </div>
        </div>
        <div class="item" id="preview">
          <div class="image">
            <svg xmlns="http://www.w3.org/2000/svg" height="24" viewBox="0 0 24 24" width="24">
              <path d="M19,3H5C3.89,3,3,3.9,3,5v14c0,1.1,0.89,2,2,2h14c1.1,0,2-0.9,2-2V5C21,3.9,20.11,3,19,3z M19,19H5V7h14V19z M12,10.5 c1.84,0,3.48,0.96,4.34,2.5c-0.86,1.54-2.5,2.5-4.34,2.5S8.52,14.54,7.66,13C8.52,11.46,10.16,10.5,12,10.5 M12,9 c-2.73,0-5.06,1.66-6,4c0.94,2.34,3.27,4,6,4s5.06-1.66,6-4C17.06,10.66,14.73,9,12,9L12,9z M12,14.5c-0.83,0-1.5-0.67-1.5-1.5 s0.67-1.5,1.5-1.5s1.5,0.67,1.5,1.5S12.83,14.5,12,14.5z" />
            </svg>
          </div>
          <div class="text">
            预览
          </div>
        </div>
      </div>
    </div>`;

    }


    static get observedAttributes() {
        return ['title'];
    }


    connectedCallback() {

        this.root.host.style.userSelect = 'none';

        // this.dispatchEvent(new CustomEvent());
        /*
        this.dispatchEvent(new CustomEvent('submit', {
                  detail: 0
              }));
              */
        const textarea = document.querySelector('textarea');
        tab(textarea);
        document.addEventListener('visibilitychange', async ev => {
            localStorage.setItem('content', textarea.value);
        });
        this.root.querySelector('#head').addEventListener('click', evt => {
            evt.stopPropagation();
            formatHead(textarea, 2);
        });
        this.root.querySelector('#english').addEventListener('click', async evt => {
            evt.stopPropagation();
            await trans(textarea, 1);
        });
        this.root.querySelector('#save').addEventListener('click', async evt => {
            evt.stopPropagation();
            saveData(textarea);
        });
        this.root.querySelector('#preview').addEventListener('click', async evt => {
            evt.stopPropagation();
            preview();
        });
        this.root.querySelector('#menu').addEventListener('click', async evt => {
            evt.stopPropagation();
            const customEditorMenu = document.createElement('custom-editor-menu');
            customEditorMenu.addEventListener('submit', async evt => {
                switch (evt.detail) {
                    case '0':
                        preview();
                        break;
                    case '1':
                        formatList(textarea);
                        break
                    case '2':
                        uploadHanlder(textarea);
                        break
                    case '3':
                        await trans(textarea, 0);
                        break;
                    case '4':
                        const customDialogTextarea = document.createElement('custom-dialog-textarea');
                        document.body.appendChild(customDialogTextarea);
                        break
                    case '5':
                        const pv = findCodeBlock(textarea);
                        navigator.clipboard.writeText(textarea.value.substring(pv[0], pv[1]));
                        break;
                    case '6':
                        const p = findCodeBlock(textarea);
                        textarea.setRangeText(await navigator.clipboard.readText(), p[0], p[1]);
                        break;
                }
            });
            document.body.appendChild(customEditorMenu);
        });
        render(textarea);
        document.addEventListener('keydown', async ev => {
            if (ev.key === 'F1') {
                ev.preventDefault();
                // textarea.setRangeText(
                //     `\`${textarea.value.substring(textarea.selectionStart, textarea.selectionEnd)}\``
                //     , textarea.selectionStart, textarea.selectionEnd)

                textarea.setRangeText(localStorage.getItem('template'))
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
                    saveData(textarea);
                } else if (ev.ctrlKey && ev.key.toLowerCase() === 's') {
                    ev.preventDefault();
                    saveData(textarea);
                } else if (ev.ctrlKey && ev.key.toLowerCase() === 'p') {
                    ev.preventDefault();
                    preview();
                } else if (ev.ctrlKey && ev.key.toLowerCase() === 'd') {
                    ev.preventDefault();
                    const pv = findCodeBlock(textarea);
                    navigator.clipboard.writeText(textarea.value.substring(pv[0], pv[1]));
                } else if (ev.ctrlKey && ev.key.toLowerCase() === 'f') {
                    ev.preventDefault();
                    const p = findCodeBlock(textarea);
                    textarea.setRangeText(await navigator.clipboard.readText(), p[0], p[1]);
                } else if (ev.ctrlKey && ev.key.toLowerCase() === 'h') {
                    ev.preventDefault();
                    formatHead(textarea, 2);
                } else if (ev.ctrlKey && ev.key.toLowerCase() === 'l') {
                    ev.preventDefault();
                    textarea.setRangeText(`\`\`\`js
${await navigator.clipboard.readText()}
\`\`\``)
                } else if (ev.ctrlKey && ev.key.toLowerCase() === 'k') {
                    ev.preventDefault();
                    window.open(
                        substringNearest(textarea.value,
                            textarea.selectionStart, '( ', ' )\r\n').trim(), '_blank'
                    );
                } else if (ev.ctrlKey && ev.key.toLowerCase() === 'o') {
                    ev.preventDefault();
                    let position = findExtendPosition(textarea);
                    const lines = textarea.value.substring(position[0], position[1])
                        .split('\n')
                        .map(x => x.trim())
                        .sort((x, y) => {
                            return x.localeCompare(y);
                        });
                    textarea.setRangeText(lines.join('\n'), position[0], position[1]);
                } else if (ev.ctrlKey && ev.key.toLowerCase() === 'm') {
                    ev.preventDefault();
                    const s = await navigator.clipboard.readText();
                    textarea.setRangeText(
                        `- [${substringAfterLast(s.trim(),"/")}](${s.trim()})`,
                        textarea.selectionStart,
                        textarea.selectionEnd
                    )
                }
            }
        });
    }

    disconnectedCallback() {

    }

    attributeChangedCallback(attrName, oldVal, newVal) {
        if (attrName === 'title') {
            this.root.querySelector('.title').textContent = newVal;
        }
    }

}

customElements.define('custom-editor-bar', CustomEditorBar);

/*
<!--\
<custom-editor-bar></custom-editor-bar>
<script src="custom-editor-bar.js"></script>

const customEditorBar = document.querySelector('custom-editor-bar');
customEditorBar.addEventListener('submit', evt => {
            evt.stopPropagation();
        });

const customEditorBar = document.createElement('custom-editor-bar');
customEditorBar.setAttribute('title','');
document.body.appendChild(customEditorBar);
this.dispatchEvent(new CustomEvent('submit', {
detail: evt.currentTarget.dataset.index
}))
-->
*/
function findCodeBlock(textarea) {
    const value = textarea.value;
    let start = textarea.selectionStart;
    let end = textarea.selectionEnd;
    while (start > -1) {
        if (value[start] === '`' && value[start - 1] === '`' && value[start - 2] === '`') {
            start += 1;
            while (start < value.length) {
                if (value[start] === '\n') {
                    start++;
                    break;
                }
                start++;
            }
            break;
        }
        start--;
    }
    while (end < value.length) {
        if (value[end] === '`' && value[end + 1] === '`' && value[end + 2] === '`') {
            end--;
            break;
        }
        end++;
    }
    return [start, end];
}

function uploadHanlder(editor) {
    if (window.location.protocol === 'https:') {
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
    } else {
        const input = document.createElement('input');
        input.type = 'file';
        input.addEventListener('change', async ev => {
            const file = input.files[0];
            const imageFile = await uploadImage(file, file.name);
            const string = `![](https://static.lucidu.cn/images/${imageFile})\n\n`;
            editor.setRangeText(string, editor.selectionStart, editor.selectionStart);
        });
        input.click();
    }

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

function tab(textarea) {
    textarea.addEventListener('keydown', function (e) {
        if (e.keyCode === 9) {
            const p = findExtendPosition(textarea);
            const start = this.selectionStart;
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

    const lines = await google(value.replace(/\d+[\r\n\s]+(\d{2}:\d{2}:\d{2},\d{3}) --> (\d{2}:\d{2}:\d{2},\d{3})/, "").trim(), english);
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
    console.log('---------------->', results)
    // string
    // \n\n${year}
    editor.setRangeText(`${english ? '' : (lines[0].join(' '))}\n${results}`, points[1], points[1]);
}

let translate = '/api/trans';

async function google(value, english) {
    // /translate
    // https://service-mayeka3y-1258705152.hk.apigw.tencentcs.com/release/
    // https://service-ehkp0lyi-1301282710.hk.apigw.tencentcs.com/release/
    const response = await fetch(`${translate}?q=${encodeURIComponent(value.trim())}&to=${english ? "zh" : "en"}`);
    const obj = await response.text();
    const lines1 = [];
    const lines2 = [];
    const translated = JSON.parse(obj.replaceAll(/您/g, '你').replaceAll(/ - /g, "——"));
    if (translated.sentences) {
        const sentences = translated.sentences;
        for (let index = 0; index < sentences.length; index++) {
            const element = sentences[index];
            //lines1.push(element.orig);
            lines2.push(element.trans);
        }
    } else {
        const trans = translated.trans_result;
        for (let index = 0; index < trans.length; index++) {
            const element = trans[index];
            // lines1.push(element.src);
            lines2.push(element.dst);
        }
    }
    return [lines1, lines2];
}

async function saveData(textarea) {
    await submitData(textarea);

}

async function submitData(textarea) {
    const firstLine = textarea.value.trim().split("\n", 2)[0];
    const obj = {

        content: substringAfter(textarea.value.trim(), "\n"),
        title: firstLine.replace(/^#+ +/, ''),
    };
    const searchParams = new URL(window.location.href).searchParams;
    const id = searchParams.get('id');
    let baseUri = (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') ? 'http://192.168.8.55:8089' : '';

    if (id) {
        obj._id = parseInt(id);
        obj.update_at = new Date().getTime();
    } else {
        obj.create_at = new Date().getTime();
        obj.update_at = new Date().getTime();
    }
    if (obj.title.indexOf('|') !== -1) {
        obj.tag = substringAfter(obj.title, '|').trim();
        obj.title = substringBefore(obj.title, '|').trim();
    }
    const response = await fetch(`${baseUri}/api/note`, {
        method: 'POST',
        body: JSON.stringify(obj)
    });
    const res = await response.text();
    if (id)
        document.getElementById('toast').setAttribute('message', '成功');
    else
        window.location = `${window.location.origin}${window.location.pathname}?id=${res}`

}

async function loadData(baseUri, id) {

    const response = await fetch(`${baseUri}/api/note?id=${id}`);
    return await response.json();
}

async function render(textarea) {
    textarea.value = localStorage.getItem("content");
    const searchParams = new URL(window.location.href).searchParams;
    const id = searchParams.get('id');
    let baseUri = (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') ? 'http://192.168.8.55:8089' : '';

    if (id) {
        try {
            const obj = await loadData(baseUri, id);
            document.title = obj.title;
            textarea.value = `# ${obj.title}|${obj.tag}
        
${obj.content.trim()}
        `
        } catch (error) {
            console.log(error)
        }
    }
}

function preview() {
    const searchParams = new URL(window.location.href).searchParams;
    const id = searchParams.get('id');
    if (window.location.protocol === 'https:')
        window.open(`https://wxyoga.cn/article?id=${id}`, '_blank');
    else
        window.open(`article.html?id=${id}`, '_blank')
}

function substringAfter(string, delimiter, missingDelimiterValue) {
    const index = string.indexOf(delimiter);
    if (index === -1) {
        return missingDelimiterValue || string;
    } else {
        return string.substring(index + delimiter.length);
    }
}
function substringAfterLast(string, delimiter, missingDelimiterValue) {
    const index = string.lastIndexOf(delimiter);
    if (index === -1) {
        return missingDelimiterValue || string;
    } else {
        return string.substring(index + delimiter.length);
    }
}

function substringNearest(string, index, start, end) {
    let j = index;
    while (j > -1) {
        if (start.indexOf(string[j]) !== -1) {
            j++
            break;
        }
        j--;
    }
    let k = index;
    while (k < string.length) {
        if (end.indexOf(string[k]) !== -1) {
            break;
        }
        k++;
    }
    return string.substring(j, k);
}

function substringBefore(string, delimiter, missingDelimiterValue) {
    const index = string.indexOf(delimiter);
    if (index === -1) {
        return missingDelimiterValue || string;
    } else {
        return string.substring(0, index);
    }
}

/*
console.log([...document.querySelectorAll('.slide-image-wrap img')]
    .map((x, index) => {
        return `${x.src.split('?')[0]}\n\tout=${index + 1}.jpg`;
    }).reverse()
    .join('\n'))
    */