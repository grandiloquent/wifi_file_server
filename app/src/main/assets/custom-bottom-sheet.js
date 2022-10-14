class CustomBottomSheet extends HTMLElement {

  constructor() {
    super();

    this.root = this.attachShadow({ mode: 'open' });

    this.root.innerHTML = `<style>.menu-item
        {
            -webkit-box-direction: normal;
            color: #030303;
            padding: 0;
            height: 48px;
            display: flex;
            -webkit-box-align: center;
            align-items: center;
        }
        #overlay
        {
            position: fixed;
            top: 0;
            bottom: 0;
            left: 0;
            right: 0;
            z-index: 10;
            cursor: pointer;
            background-color: rgba(0,0,0,.6);
        }
        #hidden-button
        {
            word-wrap: break-word;
            -webkit-text-size-adjust: 100%;
            padding: 0;
            border: none;
            outline: none;
            font: inherit;
            text-transform: inherit;
            color: inherit;
            background: transparent;
            cursor: pointer;
            position: fixed;
            top: 0;
            left: 0;
            height: 1px;
            width: 1px;
        }
        .menu-item-button
        {
            border: none;
            outline: none;
            font: inherit;
            color: inherit;
            background: transparent;
            cursor: pointer;
            box-sizing: border-box;
            text-align: initial;
            text-transform: unset;
            width: 100%;
            display: flex;
            padding: 0;
            margin-left: 12px;
            font-size: 1.6rem;
            line-height: 2.2rem;
        }</style>
            <div id="overlay">
              <button id="hidden-button">
              </button>
            </div>
            <div style="border-radius: 12px; background-color: #fff; display: block; overflow: hidden; position: fixed; margin: 0 8px 24px; bottom: 0; left: 0; right: 0; z-index: 20;">
              <div style="overflow: hidden; -webkit-box-flex: 0; flex: none; border-bottom: 1px solid #fff;">
                <div style="background: #030303; opacity: .15; border-radius: 4px; height: 4px; margin: 0 auto; width: 40px; margin-top: 8px;">
                </div>
                <div style="-webkit-box-pack: justify; justify-content: space-between; display: flex; margin-top: 8px;">
                </div>
              </div>
              <div style="-webkit-box-flex: 1; flex: 1; overflow-y: hidden; max-height: 325.2px;">
                <div style="display: flex; -webkit-box-orient: vertical; -webkit-box-direction: normal; flex-direction: column; color: #030303;">
                <div class="menu-item">
                    <button id="delete-action" class="menu-item-button">
                      删除
                    </button>
                  </div> 
                  <div class="menu-item">
                    <button id="move-action" class="menu-item-button">
                      移动
                    </button>
                  </div> 
                  
                <div class="menu-item">
                    <button id="close-action" class="menu-item-button">
                      取消
                    </button>
                  </div>
                </div>
              </div>
            </div>
		`;
    const overlay = this.root.querySelector('#overlay');
    overlay.addEventListener('click', evt => {
      this.root.host.style.display = 'none';
    })
    const closeAction = this.root.querySelector('#close-action');
    closeAction.addEventListener('click', evt => {
      this.root.host.style.display = 'none';
    })

    const deleteAction = this.root.querySelector('#delete-action');
    deleteAction.addEventListener('click', evt => {
      this.dispatchEvent(new CustomEvent('delete'));
    })
    const moveAction = this.root.querySelector('#move-action');
    moveAction.addEventListener('click', evt => {
      this.dispatchEvent(new CustomEvent('move'));
    })


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
customElements.define('custom-bottom-sheet', CustomBottomSheet);
/*
<!--\
<custom-bottom-sheet></custom-bottom-sheet>
<script src="custom-bottom-sheet.js"></script>
const customBottomSheet = document.querySelector('custom-bottom-sheet');
const customBottomSheet = document.createElement('custom-bottom-sheet');
customBottomSheet.setAttribute('data',JSON.stringify(obj));
document.body.appendChild(customBottomSheet);
-->
*/