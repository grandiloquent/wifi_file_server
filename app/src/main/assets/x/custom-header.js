class CustomHeader extends HTMLElement {

  constructor() {
    super();

    this.root = this.attachShadow({ mode: 'open' });

    this.root.innerHTML = `<style>.header
{
    height: 48px;
    display: flex;
    align-items: center;
    position: relative;
    z-index: 3;
    box-shadow: 0 4px 2px -2px rgba(0,0,0,.2);
}
.left
{
    display: flex;
    align-items: center;
    height: 48px;
}
.right
{
    display: flex;
    align-items: center;
    flex-grow: 1;
    justify-content: flex-end;
    color: #606060;
    min-width: 0;
}
.logo
{
    flex-shrink: 0;
    fill: currentColor;
    stroke: none;
    display: block;
    padding: 0 12px;
    width: 89px;
    height: 20px;
    color: #212121;
    margin-left: 0;
}
.btn
{
    box-sizing: border-box;
    padding: 12px;
    height: 48px;
    width: 48px;
}
.btn>svg
{
    width: 24px;
    height: 24px;
    fill: currentColor;
}
.wrapper
{
    display: flex;
    margin-right: 8px;
    border-radius: 32px;
    padding-right: 4px;
    height: 32px;
    flex-grow: 1;
    background: rgba(0,0,0,.05);
    align-items: center;
}
input
{
    padding: 1px 2px 4px 12px;
    border: none;
    margin: 4px 0 0 0;
    outline: none;
    font-size: 15px;
    background-color: transparent;
    flex-grow: 1;
}
.small
{
    height: 40px;
    width: 40px;
    padding: 8px;
    box-sizing: border-box;
}
.small>svg
{
    width: 24px;
    height: 24px;
}</style>
    <div class="header">
      <div class="right">
      </div>
    </div>`;

    this.header = this.root.querySelector('.header');
    this.right = this.root.querySelector('.right');

    this.show();

  }
  show() {
    this.logo = createLogo()
    this.search = createSearch();
    this.header.insertAdjacentElement('afterbegin', this.logo);
    this.right.insertAdjacentElement('beforeend', this.search);
    this.search.addEventListener('click', evt => {
      evt.stopPropagation();
      this.logo.remove();
      this.search.remove();
      this.back = createBack();
      this.wrapper = createWrapper();
      this.right.insertAdjacentElement('afterbegin', this.wrapper);
      this.back.addEventListener('click', evt => {
        this.shut();
      })
      this.header.insertAdjacentElement('afterbegin', this.back);
    });
  }
  shut() {
    this.back.remove();
    this.wrapper.remove();
    this.show();
  }

  static get observedAttributes() {
    return ['data'];
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
    if (attrName === 'data') {
      const obj = JSON.parse(newVal);
    }
  }

}
customElements.define('custom-header', CustomHeader);
/*
<!--\
<custom-header></custom-header>
<script src="custom-header.js"></script>
const customHeader = document.querySelector('custom-header');
const customHeader = document.createElement('custom-header');
customHeader.setAttribute('data',JSON.stringify(obj));
document.body.appendChild(customHeader);
-->
*/
function createLogo() {
  const left = document.createElement('div');
  left.setAttribute("class", "left");
  const logo = document.createElement('div');
  logo.setAttribute("class", "logo");
  left.appendChild(logo);
  return left;
}

function createBack() {
  const btn = document.createElement('div');
  btn.setAttribute("class", "btn");
  const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
  svg.setAttribute("xmlns", "http://www.w3.org/2000/svg");
  svg.setAttribute("enable-background", "new 0 0 24 24");
  svg.setAttribute("height", "24");
  svg.setAttribute("viewBox", "0 0 24 24");
  svg.setAttribute("width", "24");
  btn.appendChild(svg);
  const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
  path.setAttribute("d", "M21,11v1H5.64l6.72,6.72l-0.71,0.71L3.72,11.5l7.92-7.92l0.71,0.71L5.64,11H21z");
  svg.appendChild(path);
  return btn;
}

function createSearch() {
  const btn = document.createElement('div');
  btn.setAttribute("class", "btn");
  const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
  svg.setAttribute("xmlns", "http://www.w3.org/2000/svg");
  svg.setAttribute("enable-background", "new 0 0 24 24");
  svg.setAttribute("height", "24");
  svg.setAttribute("viewBox", "0 0 24 24");
  svg.setAttribute("width", "24");
  btn.appendChild(svg);
  const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
  path.setAttribute("d", "M20.87,20.17l-5.59-5.59C16.35,13.35,17,11.75,17,10c0-3.87-3.13-7-7-7s-7,3.13-7,7s3.13,7,7,7c1.75,0,3.35-0.65,4.58-1.71 l5.59,5.59L20.87,20.17z M10,16c-3.31,0-6-2.69-6-6s2.69-6,6-6s6,2.69,6,6S13.31,16,10,16z");
  svg.appendChild(path);
  return btn;
}

function createWrapper() {
  const wrapper = document.createElement('div');
  wrapper.setAttribute("class", "wrapper");
  const input = document.createElement('input');
  input.setAttribute("name", "search");
  input.setAttribute("placeholder", "在 YouTube 中搜索");
  input.setAttribute("autocomplete", "off");
  input.setAttribute("autocorrect", "off");
  input.setAttribute("spellcheck", "false");
  input.setAttribute("type", "text");
  input.setAttribute("role", "combobox");
  input.setAttribute("aria-haspopup", "false");
  input.setAttribute("aria-autocomplete", "list");
  input.setAttribute("dir", "ltr");
  wrapper.appendChild(input);
  const small = document.createElement('div');
  small.setAttribute("class", "small");
  wrapper.appendChild(small);
  const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
  svg.setAttribute("xmlns", "http://www.w3.org/2000/svg");
  svg.setAttribute("enable-background", "new 0 0 24 24");
  svg.setAttribute("height", "24");
  svg.setAttribute("viewBox", "0 0 24 24");
  svg.setAttribute("width", "24");
  small.appendChild(svg);
  const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
  path.setAttribute("d", "M20.87,20.17l-5.59-5.59C16.35,13.35,17,11.75,17,10c0-3.87-3.13-7-7-7s-7,3.13-7,7s3.13,7,7,7c1.75,0,3.35-0.65,4.58-1.71 l5.59,5.59L20.87,20.17z M10,16c-3.31,0-6-2.69-6-6s2.69-6,6-6s6,2.69,6,6S13.31,16,10,16z");
  svg.appendChild(path);
  return wrapper;
}