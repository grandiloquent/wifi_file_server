class CustomLayout extends HTMLElement {

    constructor() {
        super();

        this.root = this.attachShadow({ mode: 'open' });
        this.container = document.createElement('div');
        this.root.appendChild(this.container);

        this.container.innerHTML = CustomLayout.template();


    }


    static get observedAttributes() {
        return ['text','show'];
    }


    connectedCallback() {
        // this.dispatchEvent(new CustomEvent());
        const overlay = this.root.querySelector('#overlay');
        overlay.addEventListener('click', ev => {
            this.style.display = 'none';
        });


        const close = this.root.querySelector('#close');
        close.addEventListener('click', ev => {
            this.style.display = 'none';
        });



    }
    disconnectedCallback() {

    }

    attributeChangedCallback(attrName, oldVal, newVal) {
        if (attrName === 'show') {
            this.style.display = 'block';
        } else if (attrName === 'text') {
            this.root.querySelector('#content').textContent = newVal.trim();
        }
    }

    static template() {
        return `
        ${ CustomLayout.style()}
    <div id="overlay"  style="right: 0; bottom: 0; background-color: rgba(0,0,0,.3); visibility: visible; min-height: 100%; margin: 0 auto; opacity: 1; position: fixed; left: 0; top: 0; z-index: 990;">
    </div>
    <div style="background-color: #fff; border-radius: 16px 16px 0 0; right: 0; position: fixed; left: 0; width: 100%; z-index: 991; top: auto; height: 466.9px; bottom: 0;">
      <div style="text-align: left; box-sizing: border-box; display: flex; -webkit-box-align: center; align-items: center; width: 100%; position: absolute; top: 0; height: 46px; color: #000; padding: 16px;">
        <div style="line-height: 30rpx; position: relative; width: 100%; white-space: nowrap; overflow: hidden; color: #000; font-size: 16rpx; font-weight: 600;">
          结果
        </div>
        <div id="close" style="background: url(https://file06.c.hihonor.com/nwap/22530001/images/echannelWap/icon/ic_close_popup.png) no-repeat center; position: absolute; right: 16px; top: 50%; height: 24px; width: 24px; text-indent: -9999px; background-size: 100% 100%; margin-top: -12px; display: block; z-index: 990;">
        </div>
      </div>
      <div style="overflow: auto; padding: 0 16px 8px 16px; padding-top: 0; max-height: none; position: absolute; top: 40px; bottom: 45px; left: 0; right: 0; background-color: #fff;">
        <pre style="padding: 20px 0 0 0; border: 0;">
        <code  id="content"></code>
        </pre>
      </div>
    </div>
   `;
    }

    static style() {
        return `
        <style>
       
        </style>`;
    }


}
customElements.define('custom-layout', CustomLayout);
/*
<!--
<script src="layout.js"></script>
<custom-layout></custom-layout>
-->
 */