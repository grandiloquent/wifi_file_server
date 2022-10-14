class CustomDialog extends HTMLElement {

    constructor() {
        super();

        this.root = this.attachShadow({ mode: 'open' });

        this.root.innerHTML = `<style>.dialog-container
{
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    -webkit-box-align: center;
    align-items: center;
    -webkit-box-pack: center;
    justify-content: center;
    z-index: 4;
    margin: 0 40px;
    padding: 0 env(safe-area-inset-right) env(safe-area-inset-bottom) env(safe-area-inset-left);
}
.dialog
{
    position: relative;
    z-index: 2;
    display: flex;
    -webkit-box-orient: vertical;
    -webkit-box-direction: normal;
    flex-direction: column;
    max-height: 100%;
    box-sizing: border-box;
    padding: 16px;
    margin: 0 auto;
    overflow-x: hidden;
    overflow-y: auto;
    font-size: 1.3rem;
    color: #030303;
    background-color: #f9f9f9;
    border: none;
    min-width: 250px;
    max-width: 356px;
}
.dialog-header
{
    display: flex;
    -webkit-box-orient: vertical;
    -webkit-box-direction: normal;
    flex-direction: column;
    flex-shrink: 0;
}
.dialog-body
{
    white-space: pre-wrap;
    overflow-y: auto;
    overflow-x: hidden;
    max-height: 100vh;
}
.dialog-buttons
{
    display: flex;
    -webkit-box-align: center;
    align-items: center;
    -webkit-box-pack: end;
    justify-content: flex-end;
    margin-top: 12px;
}
.button-renderer
{
    display: flex;
    -webkit-box-pack: center;
    justify-content: center;
    box-sizing: border-box;
    user-select: none;
    min-width: 5.14em;
    font-size: 1.4rem;
    text-transform: uppercase;
    border-radius: 3px;
    position: relative;
    margin: -.7em -.57em;
    color: #606060;
}
.material-button
{
    border: none;
    outline: none;
    font: inherit;
    text-transform: inherit;
    color: inherit;
    background: transparent;
    cursor: pointer;
    padding: .7em .57em;
}</style>
    <div class="dialog-container">
      <div class="dialog">
        <div class="dialog-header">
        </div>
        <div class="dialog-body">
        </div>
        <div class="dialog-buttons">
          <div class="button-renderer">
            <div id="close" class="material-button">
              取消
            </div>
          </div>
          <div class="button-renderer">
            <div id="submit" class="material-button">
              确定
            </div>
          </div>
        </div>
      </div>
      <div id="overlay" style="position: fixed; top: 0; bottom: 0; left: 0; right: 0; z-index: 1; cursor: pointer; background-color: rgba(0,0,0,.8);">
      </div>
    </div>`;

        const close = this.root.querySelector('#close');
        close.addEventListener('click', evt => {
            this.remove();
        })
        const overlay = this.root.querySelector('#overlay');
        overlay.addEventListener('click', evt => {
            this.remove();
        })
        const submit = this.root.querySelector('#submit');
        submit.addEventListener('click', evt => {
            this.dispatchEvent(new CustomEvent('submit'))
        })




    }


    static get observedAttributes() {
        return ['title', 'message'];
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
            this.root.querySelector('.dialog-header').textContent = newVal;
        } else if (attrName === 'message') {
            this.root.querySelector('.dialog-body').textContent = newVal;
        }
    }

}
customElements.define('custom-dialog', CustomDialog);
/*
<!--\
<custom-dialog></custom-dialog>
<script src="custom-dialog.js"></script>
const customDialog = document.querySelector('custom-dialog');
const customDialog = document.createElement('custom-dialog');
customDialog.setAttribute('data',JSON.stringify(obj));
document.body.appendChild(customDialog);
-->
*/