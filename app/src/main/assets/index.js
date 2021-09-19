async function getFiles(v)
{
    const resposne = await fetch('/api/files' + (v ? '?v=' + v : ''));
    return resposne.json()
}
async function loadFiles(v)
{
    const files = await getFiles(v);
    render(files)
}
const container = document.querySelector('.container');
function substringAfterLast(string, delimiter, missingDelimiterValue)
{
    const index = string.lastIndexOf(delimiter);
    if (index === -1)
    {
        return missingDelimiterValue || string
    }
    else
    {
        return string.substring(index + delimiter.length)
    }
}
function humanFileSize(size)
{
    if (size === 0)
        return '0';
    var i = Math.floor(Math.log(size) / Math.log(1024));
    return (size / Math.pow(1024, i)).toFixed(2) * 1 + ' ' + ['B', 'kB', 'MB', 'GB', 'TB'][i]
}
;
function render(items)
{
    const results = [];
    items.forEach(obj =>
        {
            const filename = substringAfterLast(obj.path, "/");
            const thumb = obj.isDirectory ? 'icon-file-m' : (/\.(?:jpg|png|jpeg)$/i.test(obj.path) && 'icon-pic-m') || (/\.(?:psd)$/i.test(obj.path) && 'icon-ps-m') || (/\.(?:mp4)$/i.test(obj.path) && 'icon-video-m') || (/\.(?:mp3|m4a)$/i.test(obj.path) && 'icon-audio-m') || (/\.(?:zip|7z|rar)$/i.test(obj.path) && 'icon-zip-m') || (/\.(?:pdf)$/i.test(obj.path) && 'icon-pdf-m') || (/\.(?:txt)$/i.test(obj.path) && 'icon-txt-m') || (/\.(?:java|htm|html|css|js|cs|c)$/i.test(obj.path) && 'icon-code-m') || 'icon-nor-m';
            const fileSize = obj.isDirectory ? '' : humanFileSize(obj.size);
            const item = `<div class="list-group-item" data-type="${obj.isDirectory ? '1' : '0'}" data-path="${encodeURIComponent(obj.path)}">
      <div class="item-inner">
        <div class="item-tit">
          <div class="thumb">
            <i class="icon icon-m ${thumb}">
            </i>
          </div>
          <div class="info">
            <a href="javascript:void(0)" class="tit">
              ${filename}
            </a>
          </div>
        </div>
        <div class="item-info">
          <!--<span class="item-info-list">
            <span class="txt txt-time">
              2019年07月25日
            </span>
          </span>--><span class="item-info-list">
            <span class="txt txt-size">
              ${fileSize}</span>
          </span>
        </div>
      </div>
    </div>
  `;
            results.push(item)
        });
    container.innerHTML = results.join('');
    const listGroupItem = document.querySelectorAll('.list-group-item');
    listGroupItem.forEach(x =>
        {
            x.addEventListener('click', async ev =>
                {
                    const element = ev.currentTarget;
                    if (element.dataset.type === "1")
                        location.href = `?v=${element.dataset.path || ''}`;
                    else
                        location.href = `/api/files?v=${element.dataset.path || ''}`
                })
        })
}
loadFiles(new URL(location.href).searchParams.get('v') || '');
function createInputFile()
{
    const input = document.createElement('input');
    input.type = 'file';
    input.style.position = 'fixed';
    input.style.transform = 'translateY(-100%)';
    input.style.top = '0';
    document.body.appendChild(input);
    return input
}
async function uploadFile(file)
{
    const formData = new FormData;
    formData.append('file', file, file.name);
    const response = await fetch('/post', {
            method: 'POST',
            body: formData
        });
    return response.ok
}
const toast = document.querySelector('custom-toast');
async function handleUploadFile(ev)
{
    const file = ev.target.files[0];
    const response = await uploadFile(file);
    if (response)
        toast.setAttribute('message', '文件已成功上传')
}
const modActionWrap = document.querySelector('.mod-action-wrap');
modActionWrap.addEventListener('click', ev =>
    {
        const input = createInputFile();
        input.click();
        input.addEventListener('change', handleUploadFile)
    });
var dropZone = document.querySelector('body');
dropZone.addEventListener('dragover', function(e)
{
    e.stopPropagation();
    e.preventDefault();
    e.dataTransfer.dropEffect = 'copy'
});
dropZone.addEventListener('drop', function(e)
{
    e.stopPropagation();
    e.preventDefault();
    uploadFiles(e.dataTransfer.files)
});
async function uploadFiles(files)
{
    document.querySelector('.dialog').className = 'dialog dialog-show';
    const dialogContext = document.querySelector('.dialog-content span');
    const length = files.length;
    let i = 1;
    for (let file of files)
    {
        dialogContext.textContent = `正在上传 (${i++}/${length}) ${file.name} ...`;
        const formData = new FormData;
        formData.append('file', file, file.name);
        try
        {
            await fetch("/post",
            {
                method: "POST",
                body: formData
            })
            .then(res => console.log(res));
        }
        catch(e) {}
    }
     document.querySelector('.dialog').className = 'dialog';
}