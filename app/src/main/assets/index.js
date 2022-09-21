let baseUri = window.location.hostname === '127.0.0.1' ? 'http://192.168.8.55:12345' : ''

async function getFiles(v) {
    const resposne = await fetch(`${baseUri}/api/files` + (v ? '?v=' + v : ''));
    return resposne.json()
}
async function loadFiles(v) {
    try {
        const files = await getFiles(v);
        render(files)
    }
    catch (error) {
        toast.setAttribute('message', '空目录或无法连接服务器')
    }
}
function substringAfterLast(string, delimiter, missingDelimiterValue) {
    const index = string.lastIndexOf(delimiter);
    if (index === -1) {
        return missingDelimiterValue || string
    }
    else {
        return string.substring(index + delimiter.length)
    }
}
function humanFileSize(size) {
    if (size === 0)
        return '0';
    var i = Math.floor(Math.log(size) / Math.log(1024));
    return (size / Math.pow(1024, i)).toFixed(2) * 1 + ' ' + ['B', 'kB', 'MB', 'GB', 'TB'][i]
}
function render(items) {
    const results = [];
    items.forEach(obj => {
        const filename = substringAfterLast(obj.path, "/");
        const thumb = obj.isDirectory ? 'icon-file-m' : (/\.(?:jpg|png|jpeg)$/i.test(obj.path) && 'icon-pic-m') || (/\.(?:psd)$/i.test(obj.path) && 'icon-ps-m') || (/\.(?:mp4)$/i.test(obj.path) && 'icon-video-m') || (/\.(?:mp3|m4a)$/i.test(obj.path) && 'icon-audio-m') || (/\.(?:zip|7z|rar)$/i.test(obj.path) && 'icon-zip-m') || (/\.(?:pdf)$/i.test(obj.path) && 'icon-pdf-m') || (/\.(?:txt)$/i.test(obj.path) && 'icon-txt-m') || (/\.(?:java|htm|html|css|js|cs|c)$/i.test(obj.path) && 'icon-code-m') || 'icon-nor-m';
        const fileSize = obj.isDirectory ? '' : humanFileSize(obj.size);
        const item = `<div class="list-group-item" data-type="${obj.isDirectory ? '1' : '0'}" data-path="${encodeURIComponent(obj.path)}">


            <i class="icon icon-m ${thumb}"></i>

          <div class="info">
            <h3>${filename}</h3>
            <span class="txt txt-size">${fileSize}</span>
          </div>
        <div class="item-info">
          <i class="icon icon-m icon-more-m"></i>
        </div>
        </div>
  `;
        results.push(item)
    });
    container.innerHTML = results.join('');
    const listGroupItem = document.querySelectorAll('.list-group-item');
    listGroupItem.forEach(x => {
        x.addEventListener('click', async ev => {
            const element = ev.currentTarget;
            if (element.dataset.type === "1")
                location.href = `?v=${element.dataset.path || ''}`;
            else
                location.href = `/api/files?v=${element.dataset.path || ''}`
        });
        const itemInfo = x.querySelector('.item-info');
        if (itemInfo) {
            itemInfo.addEventListener('click', ev => {
                ev.stopPropagation();
                const fullpath = ev.currentTarget.parentNode.dataset.path;
                let input;
                dialog.show((text1, text2) => {
                    input = text2;
                    text1.value = substringAfterLast(decodeURIComponent(fullpath), '/')
                }, async () => {
                    const resposne = await fetch(`/api/file?old=${fullpath}&new=${encodeURIComponent(input.value.trim())}`);
                    dialog.hide();
                    toast.setAttribute('message', '成功重命名')
                })
            })
        }
    })
}
function createInputFile() {
    const input = document.createElement('input');
    input.type = 'file';
    input.style.position = 'fixed';
    input.style.transform = 'translateY(-100%)';
    input.style.top = '0';
    document.body.appendChild(input);
    return input
}
async function uploadFile(file) {
    const formData = new FormData;
    formData.append('file', file, file.name);
    const v = new URL(location.href).searchParams.get('v');
    if (v)
        formData.append('v', v);
    const response = await fetch('/post', {
        method: 'POST',
        body: formData
    });
    return response.ok
}
const toast = document.querySelector('custom-toast');
async function handleUploadFile(ev) {
    const file = ev.target.files[0];
    const response = await uploadFile(file);
    if (response)
        toast.setAttribute('message', '文件已成功上传')
}
const modActionWrap = document.querySelector('.mod-action-wrap');
modActionWrap.addEventListener('click', ev => {
    const input = createInputFile();
    input.click();
    input.addEventListener('change', handleUploadFile)
});
var dropZone = document.querySelector('body');
dropZone.addEventListener('dragover', function (e) {
    e.stopPropagation();
    e.preventDefault();
    e.dataTransfer.dropEffect = 'copy'
});
dropZone.addEventListener('drop', function (e) {
    e.stopPropagation();
    e.preventDefault();
    uploadFiles(e.dataTransfer.files)
});
async function uploadFiles(files) {
    document.querySelector('.dialog').className = 'dialog dialog-show';
    const dialogContext = document.querySelector('.dialog-content span');
    const length = files.length;
    let i = 1;
    for (let file of files) {
        dialogContext.textContent = `正在上传 (${i++}/${length}) ${file.name} ...`;
        const formData = new FormData;
        formData.append('file', file, file.name);
        const v = new URL(location.href).searchParams.get('v');
        if (v)
            formData.append('v', v);
        try {
            await fetch("/post", {
                method: "POST",
                body: formData
            }).then(res => console.log(res))
        }
        catch (e) { }
    }
    document.querySelector('.dialog').className = 'dialog'
}
const container = document.querySelector('.container');
loadFiles(new URL(location.href).searchParams.get('v') || '');
const storage = document.querySelector('#storage');
if (storage) {
    storage.addEventListener('click', ev => {
        location.href = `?v=${storage.dataset.path || ''}`
    })
}
fetch(`${baseUri}/api/storage`).then(res => {
    return res.text()
}).then(res => {
    if (res) {
        storage.setAttribute('data-path', res);
        storage.parentNode.removeAttribute('style');
        storage.querySelector('span').textContent = res
    }
});

function initializeFavorites() {
    const c3Overlay = document.querySelector('.c3-overlay');
    c3Overlay.addEventListener('click', evt => {
        evt.stopPropagation();
        c3Overlay.style.display = 'none';
        bottomSheetLayout.style.display = 'none';
    });
    const bottomSheetLayout = document.querySelector('.bottom-sheet-layout');

    const favorites = document.querySelector('#favorites');
    favorites.addEventListener('click', evt => {
        evt.stopPropagation();
        c3Overlay.style.display = 'block';
        bottomSheetLayout.style.display = 'block';
    });
     
    bottomSheetLayout.querySelectorAll('.menu-item')
        .forEach(x => {
            x.addEventListener('click', evt => {
                evt.stopPropagation();
                window.location=`${baseUri}/browser?v=${x.dataset.src}`
            });
        })
}

initializeFavorites();