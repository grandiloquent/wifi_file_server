let baseUri = (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') ? 'http://192.168.8.55:8089' : '';
const mode = new URL(window.location).searchParams.get("m");

async function loadData() {
    const items = await fetch(`${baseUri}/api/notes`);
    return (await items.json()).sort((x, y) => {
        return x.update_at - y.update_at;
    });
}

async function render() {
    const items = await loadData();
    const container = document.getElementById('items');
    container.innerHTML = '';
    for (const item of items) {
        const template = mode === '1' ?
            `<div class="text-line-container" data-type="${item.isDir}" data-path="${item.path}" data-name="${item.name}" >
    <div class="text-line-wrapper">
        <div class="text-line">
            ${item.name}
        </div>
        <div class="icon-more_vert" style="width: 24px">
        <svg  viewBox="0 0 24 24">
<path d="M12 15.984q0.797 0 1.406 0.609t0.609 1.406-0.609 1.406-1.406 0.609-1.406-0.609-0.609-1.406 0.609-1.406 1.406-0.609zM12 9.984q0.797 0 1.406 0.609t0.609 1.406-0.609 1.406-1.406 0.609-1.406-0.609-0.609-1.406 0.609-1.406 1.406-0.609zM12 8.016q-0.797 0-1.406-0.609t-0.609-1.406 0.609-1.406 1.406-0.609 1.406 0.609 0.609 1.406-0.609 1.406-1.406 0.609z"></path>
</svg>
        </div>
    </div>
</div>
` : `<div class="text-line-container" data-id="${item.id}"  data-hidden="${item.hidden}">
    <div class="text-line-wrapper">
        <div class="text-line" style="${item.hidden === 0 ? '' : 'color: red'}">
            ${item.title}
        </div> 
        <div class="icon-more_vert" style="width: 24px">
        <svg  viewBox="0 0 24 24">
<path d="M12 15.984q0.797 0 1.406 0.609t0.609 1.406-0.609 1.406-1.406 0.609-1.406-0.609-0.609-1.406 0.609-1.406 1.406-0.609zM12 9.984q0.797 0 1.406 0.609t0.609 1.406-0.609 1.406-1.406 0.609-1.406-0.609-0.609-1.406 0.609-1.406 1.406-0.609zM12 8.016q-0.797 0-1.406-0.609t-0.609-1.406 0.609-1.406 1.406-0.609 1.406 0.609 0.609 1.406-0.609 1.406-1.406 0.609z"></path>
</svg>
        </div>
    </div>
</div>
`

        container.insertAdjacentHTML('afterend', template);

    }

    document.querySelectorAll('.text-line-container')
        .forEach(x => {
            x.addEventListener('click', ev => {
                window.location = `../editor?id=${ev.currentTarget.dataset.id}&hidden=${ev.currentTarget.dataset.hidden}`;
            })
            x.querySelector('.icon-more_vert')
                .addEventListener('click', ev => {
                    ev.stopPropagation();
                    const popup = document.getElementById('popup');

                    popup.style.display = 'block';
                    popup.querySelector('.overlay').addEventListener('click', () => {
                        popup.style.display = 'none';
                    });
                    const p = ev.currentTarget.parentNode.parentNode.dataset.path + "/" + ev.currentTarget.parentNode.parentNode.dataset.name;
                    document.getElementById('action-delete')
                        .addEventListener('click', async () => {
                            const response = await fetch(`/api/source/7?p=${p}`);
                            popup.style.display = 'none';
                        });
                })
        });

    window.onhashchange = () => {
        render();
    }
}

render();