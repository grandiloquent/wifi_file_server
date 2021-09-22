(function () {

    class Dialog {

        constructor() {
            this.initializeTemplate();
            const container = document.createElement('div');
            container.attachShadow({ mode: 'open' });
            container.shadowRoot.append(this.template.content.cloneNode(true));
            container.style.display = 'none';
            const host = container.shadowRoot.host;

            document.body.appendChild(container);

            const dialogContainer = container.shadowRoot.querySelector('.dialog-container');

            const c3Overlay = container.shadowRoot.querySelector('c3-overlay');

            c3Overlay.addEventListener('click', ev => {
                host.style.display = 'none';
            });

            container.shadowRoot.querySelector('#close').addEventListener('click', ev => {
                host.style.display = 'none';
            });
            this.host = host;
            this.text1 = container.shadowRoot.querySelector('#text1');
            this.text2 = container.shadowRoot.querySelector('#text2');
            this.ok = container.shadowRoot.querySelector('#ok');

        }

        initializeTemplate() {
            const template = document.createElement('template');

            template.innerHTML = `

    <style>
    .dialog-container
    {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        display: -webkit-box;
        display: -webkit-flex;
        display: flex;
        -webkit-box-align: center;
        -webkit-align-items: center;
        align-items: center;
        -webkit-box-pack: center;
        -webkit-justify-content: center;
        justify-content: center;
        z-index: 3;
    }
    @media(min-width: 330px) and (min-height: 330px)
    {
        .dialog-container
        {
            margin: 0 40px;
            padding: 0;
            padding: 0 env(safe-area-inset-right) env(safe-area-inset-bottom) env(safe-area-inset-left);
        }
    }
    .dialog
    {
        position: relative;
        z-index: 2;
        display: -webkit-box;
        display: -webkit-flex;
        display: flex;
        -webkit-box-orient: vertical;
        -webkit-flex-direction: column;
        flex-direction: column;
        max-height: 100%;
        max-width: 100%;
        -webkit-box-sizing: border-box;
        box-sizing: border-box;
        padding: 16px;
        margin: 0 auto;
        overflow-x: hidden;
        overflow-y: auto;
        font-size: 13px;
        color: #030303;
        background-color: #f9f9f9;
        border: none;
    }
    @media(min-width: 330px) and (min-height: 330px)
    {
        .dialog
        {
            min-width: 250px;
            max-width: 356px;
        }
    }
    c3-overlay
    {
        position: fixed;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
        z-index: 1;
        cursor: pointer;
        background-color: rgba(0,0,0,.8);
    }
    button
    {
        padding: 0;
        border: none;
        outline: none;
        font: inherit;
        text-transform: inherit;
        color: inherit;
        background: transparent;
    }
    button, select, [role="button"], input[type="checkbox"]
    {
        cursor: pointer;
    }
    .hidden-button
    {
        position: fixed;
        top: 0;
        left: 0;
        height: 1px;
        width: 1px;
    }
    .dialog-header
    {
        display: -webkit-box;
        display: -webkit-flex;
        display: flex;
        -webkit-flex-direction: column;
        flex-direction: column;
        -webkit-flex-shrink: 0;
        flex-shrink: 0;
    }
    h1, h2, h3, h4
    {
        margin: 0 0 3px 0;
    }
    h1, h2, h3, h4, .subhead
    {
        box-orient: vertical;
        -webkit-box-orient: vertical;
        display: box;
        display: -webkit-box;
        max-height: 2.5em;
        -webkit-line-clamp: 2;
        overflow: hidden;
        line-height: 1.25;
        text-overflow: ellipsis;
        font-weight: normal;
    }
    h2
    {
        font-size: 18px;
    }
    .user-text
    {
        white-space: pre-wrap;
    }
    .dialog-body
    {
        overflow-y: auto;
        overflow-x: hidden;
        max-height: 300px;
        max-height: 100vh;
    }
    p
    {
        margin: 8px 0;
        line-height: 1.25;
    }
    .secondary-text
    {
        color: #606060;
    }
    .dialog-buttons
    {
        -webkit-flex-shrink: 0;
        flex-shrink: 0;
        display: -webkit-box;
        display: -webkit-flex;
        display: flex;
        -webkit-box-pack: end;
        -webkit-justify-content: flex-end;
        justify-content: flex-end;
        -webkit-box-align: center;
        -webkit-align-items: center;
        align-items: center;
        margin-top: 12px;
    }
    c3-material-button
    {
        display: -webkit-box;
        display: -webkit-flex;
        display: flex;
        -webkit-box-pack: center;
        -webkit-justify-content: center;
        justify-content: center;
        -webkit-box-sizing: border-box;
        box-sizing: border-box;
        -webkit-user-select: none;
        min-width: 5.14em;
        margin: 0 .29em;
        font-size: 14px;
        text-transform: uppercase;
        -webkit-border-radius: 3px;
        border-radius: 3px;
    }
    c3-material-button.compact
    {
        margin: -.7em -.57em;
    }
    .c3-material-button-button
    {
        padding: .7em .57em;
    }
    .cbox, .vbox, .center
    {
        display: -webkit-box;
        display: -webkit-flex;
        display: flex;
        -webkit-box-align: center;
        -webkit-align-items: center;
        align-items: center;
    }
    input
    {
        background-color: transparent;
        padding-bottom: 4px;
        outline: none;
        -webkit-box-sizing: border-box;
        box-sizing: border-box;
        border: none;
        -webkit-border-radius: 0;
        border-radius: 0;
        margin-bottom: 1px;
        font: inherit;
        color: #030303;
    }
    input
    {
        border-bottom-style: solid;
        border-bottom-width: 1px;
        border-bottom-color: #737373;
        text-overflow: ellipsis;
        -webkit-box-flex: 1;
        -webkit-flex-grow: 1;
        flex-grow: 1;
        margin: 0;
        font-size: 15px;
        font-weight: normal;
        width: 100%;
        margin-top: 4px;
        outline: none;
        line-height: normal;
        font-family: Roboto,Arial,sans-serif;
    }
    </style>
    <div class="dialog-container">
      <dialog role="dialog" aria-modal class="dialog">
        <div id="dialog-header:3" class="dialog-header" aria-live="polite" tabindex="0">
          <h2 class="modal-title">重命名</h2>
        </div>
        <div id="dialog-body:4" class="dialog-body">
        <input type="text" id="text1" autocomplete="off" autocorrect="off" spellcheck="false">
        <input type="text" id="text2" autocomplete="off" autocorrect="off" spellcheck="false">
        </div>
        <div class="dialog-buttons">
          <c3-material-button class="compact">
            <button class="c3-material-button-button" id="close">
              <div class="cbox">
                取消
              </div>
            </button>
          </c3-material-button>
          <c3-material-button class="button-renderer compact" data-style="STYLE_BRAND" data-icon-only="false" is-busy="false" aria-busy="false" disabled="false" data-button-id="">
            <button class="c3-material-button-button" id="ok">
              <div class="cbox">
                <div class="button-renderer-text">
                    确定
                </div>
              </div>
            </button>
          </c3-material-button>
        </div>
      </dialog>
      <c3-overlay>
        <button class="hidden-button" aria-label="close">
        </button>
      </c3-overlay>
    </div>
    `;

            document.body.appendChild(template);
            this.template = template;
        }

        show(handler, listener) {
            if (handler) {
                handler(this.text1, this.text2);
            }
            if (listener)
                this.ok.addEventListener('click', listener);
            this.host.removeAttribute('style');
        }
        
        hide(){
            this.host.style.display = 'none';
        }
    }

    window.dialog = new Dialog();
})();