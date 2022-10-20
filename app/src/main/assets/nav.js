class CustomNav extends HTMLElement {

    constructor() {
        super();

        this.root = this.attachShadow({mode: 'open'});
        this.container = document.createElement('div');
        this.root.appendChild(this.container);

        this.container.innerHTML = CustomNav.template();
        this.isShowing = false;
    }


    static get observedAttributes() {
        return ['show'];
    }


    connectedCallback() {
        const container = this.root.querySelector('.container');
        container.style.display = 'none';
        container.addEventListener('click', ev => {
            this.root.querySelector('.wrapper').style.transform = 'translateX(0px)';
        })
        this.root.querySelector('.wrapper').addEventListener('transitionend', ev => {
            if (this.isShowing) {
                container.style.display = 'none';
            }
            this.isShowing = !this.isShowing;
        });
        this.iconPostAdd = this.root.querySelector('#icon-post_add');
        this.iconPostAdd.addEventListener('click', ev => {
            this.dispatchEvent(new CustomEvent('newfile'));
        })
    }

    disconnectedCallback() {

    }

    attributeChangedCallback(attrName, oldVal, newVal) {
        if (attrName === 'show') {
            this.root.querySelector('.container')
                .style.display = 'block';

            requestAnimationFrame(() => {
                this.root.querySelector('.wrapper').style.transform = 'translateX(250px)';
            })
        }
    }

    static template() {
        return `
        ${CustomNav.style()}

    <div class="container">
      <div class="wrapper">
        <div style="font-family: Roboto,Helvetica Neue,Arial,sans-serif; -webkit-text-size-adjust: none; font-size: 16px; color: #1558d6; text-decoration: none; -webkit-tap-highlight-color: rgba(0,0,0,.1); background: url(/images/nav_logo325_hr.png) no-repeat; background-position: 0 -374px; height: 36px; width: 92px; background-size: 167px; display: block; margin-left: 15px; padding-bottom: 8px; outline: 0;">
        </div>
        <div id="icon-post_add" style="font-family: Roboto,Helvetica Neue,Arial,sans-serif; -webkit-text-size-adjust: none; font-size: 16px; text-decoration: none; -webkit-tap-highlight-color: rgba(0,0,0,.1); display: flex; align-items: center; color: rgba(0,0,0,.54); height: 48px; line-height: 20px; width: 100%; vertical-align: middle; outline: 0;">
          <div style="padding: 0 15px; margin-bottom: 3px; width: 24px; vertical-align: middle; height: 24px;">
          <svg  viewBox="0 0 24 24">
<path d="M6.984 15h8.016v2.016h-8.016v-2.016zM6.984 12h8.016v2.016h-8.016v-2.016zM6.984 9h8.016v2.016h-8.016v-2.016zM18.984 2.016v3h3v1.969h-3v3h-1.969v-3h-3v-1.969h3v-3h1.969zM17.016 19.219v-7.219h1.969v6.984q0 0.797-0.586 1.406t-1.383 0.609h-12q-0.797 0-1.406-0.609t-0.609-1.406v-12q0-0.797 0.609-1.383t1.406-0.586h6.984v1.969h-6.984v12.234h12z"></path>
</svg>
          </div>
          <div>
            新建文件
          </div>
        </div>
      </div>
    </div>
   `;
    }

    static style() {
        return `
        <style>
         .wrapper
{
    
    background-color: #fff;
    height: 100%;
    font-size: 16px;
    left: -250px;
    outline: none;
    overflow-y: scroll;
    padding-top: 15px;
    position: fixed;
    top: 0;
    transition: .5s;
    width: 250px;
    z-index: 200;
   
}
            
   .container
{
    height: 100%;
    overflow: hidden;
    position: fixed;
    top: 0;
    width: 100vw;
    z-index: 199;
    display: block;
    background-color: rgba(0,0,0,.6);
    
}
           
        </style>`;
    }


}

customElements.define('custom-nav', CustomNav);
// <script src="nav.js"></script>
// <custom-nav></custom-nav>