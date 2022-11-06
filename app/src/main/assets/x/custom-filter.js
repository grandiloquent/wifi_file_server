class CustomFilter extends HTMLElement {

    constructor() {
        super();

        this.root = this.attachShadow({ mode: 'open' });

        this.root.innerHTML = `<style>.title
{
    display: inline-block;
    padding-right: 14px;
    white-space: nowrap;
}
.arrow
{
    border-style: solid;
    border-width: 4px 4px 0 4px;
    width: 0;
    height: 0;
    margin-left: -10px;
    top: 50%;
    margin-top: -2px;
    position: absolute;
    border-color: #202124 transparent;
}
.container
{
    background-color: #fff;
    border: 0;
    border-radius: 0;
    box-shadow: 0 0 0 1px #ebedef;
    margin: 0 0 8px;
}
.wrapper
{
    display: block;
    padding: 11px 16px 11px;
    color: #70757a;
    font-size: small;
    position: relative;
    z-index: 126;
}
.item
{
    display: inline-block;
    position: relative;
    padding-top: 0;
    padding-bottom: 0;
    padding-right: 16px;
    line-height: 22px;
    cursor: pointer;
    color: #202124;
    padding-left: 0;
}
.menu
{
    background-color: #fff;
    border: none;
    display: block;
    white-space: nowrap;
    border-radius: 8px;
    padding: 5px 0;
    outline: 0;
}
.menu-item
{
    background-position: left center;
    background-repeat: no-repeat;
    position: relative;
    display: block;
    font-size: 14px;
    line-height: 23px;
    white-space: nowrap;
}
.menu-item.selected
{
    background-color: rgba(0,0,0,.1);
    background-image: url(//ssl.gstatic.com/ui/v1/menu/checkmark.png);
}
.dropmenu
{
    display: block;
    position: absolute;
    border-radius: 8px;
    box-shadow: 0 2px 10px 0 rgba(0,0,0,.2);
    z-index: 200;
    left: 0;
    top: 0;
}
.menu-wrapper
{
    vertical-align: middle;
    line-height: 48px;
    overflow: hidden;
    text-overflow: ellipsis;
    padding: 0;
}
.menu-container
{
    padding-left: 32px;
    padding-right: 32px;
    display: block;
    padding-top: 4px;
    padding-bottom: 4px;
    cursor: pointer;
    outline: 0;
}</style>
    <div class="container">
      <div class="wrapper">
        <div class="item">
          <div class="title">
            类型
          </div><span class="arrow">
          </span>
        </div>
      </div>
    </div>`;
        const item = this.root.querySelector('.item');
        let itemMenu;
        item.addEventListener('click', evt => {
            evt.stopPropagation();
            if (itemMenu) {
                itemMenu.remove();
                itemMenu = null;
                return
            }
            const element = createDropMenu();
            itemMenu = element;
            this.root.querySelector('.wrapper').insertAdjacentElement(
                'beforeend', element
            )
            element.style.top = (item.getBoundingClientRect().height + 11) + 'px';
            element.style.left = (item.getBoundingClientRect().x) + 'px';

        });
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
    }
    disconnectedCallback() {

    }

    attributeChangedCallback(attrName, oldVal, newVal) {
        if (attrName === 'title') {
            this.root.querySelector('.title').textContent = newVal;
        }
    }

}
customElements.define('custom-filter', CustomFilter);
/*
<!--\
<custom-filter></custom-filter>
<script src="custom-filter.js"></script>

const customFilter = document.querySelector('custom-filter');
customFilter.addEventListener('submit', evt => {
            evt.stopPropagation();
        });

const customFilter = document.createElement('custom-filter');
customFilter.setAttribute('title','');
document.body.appendChild(customFilter);
this.dispatchEvent(new CustomEvent('submit', {
detail: evt.currentTarget.dataset.index
}))
-->
*/
function createDropMenu() {
    const dropmenu = document.createElement('div');
    dropmenu.setAttribute("class", "dropmenu");
    const menu = document.createElement('div');
    menu.setAttribute("class", "menu");
    dropmenu.appendChild(menu);
    const menuItem = document.createElement('div');
    menuItem.setAttribute("class", "menu-item");
    menu.appendChild(menuItem);
    const menuWrapper = document.createElement('div');
    menuWrapper.setAttribute("class", "menu-wrapper");
    menuItem.appendChild(menuWrapper);
    const menuContainer = document.createElement('div');
    menuContainer.setAttribute("class", "menu-container");
    menuWrapper.appendChild(menuContainer);
    menuContainer.textContent = `XVideos`;
    return dropmenu
}