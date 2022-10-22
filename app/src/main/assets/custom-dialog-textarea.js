class CustomDialogTextarea extends HTMLElement {

    constructor() {
        super();
				
        this.root = this.attachShadow({mode: 'open'});

this.root.innerHTML=`
		<style>
        </style>
		`;
    }

 
    static get observedAttributes() {
        return ['title'];
    }
  

    connectedCallback() {
		
this.root.host.style.userSelect='none';
        
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
customElements.define('custom-dialog-textarea', CustomDialogTextarea);
/*
<!--\
<custom-dialog-textarea></custom-dialog-textarea>
<script src="custom-dialog-textarea.js"></script>

const customDialogTextarea = document.querySelector('custom-dialog-textarea');
customDialogTextarea.addEventListener('submit', evt => {
            evt.stopPropagation();
        });

const customDialogTextarea = document.createElement('custom-dialog-textarea');
customDialogTextarea.setAttribute('title','');
document.body.appendChild(customDialogTextarea);
this.dispatchEvent(new CustomEvent('submit', {
detail: evt.currentTarget.dataset.index
}))
-->
*/