!function(e,n){"object"==typeof exports&&"undefined"!=typeof module?module.exports=n():"function"==typeof define&&define.amd?define(n):(e=e||self).markdownItAnchor=n()}(this,function(){var e=!1,n={false:"push",true:"unshift",after:"push",before:"unshift"},t={isPermalinkSymbol:!0};function r(r,i,a,l){var o;if(!e){var c="Using deprecated markdown-it-anchor permalink option, see https://github.com/valeriangalliat/markdown-it-anchor#todo-anchor-or-file";"object"==typeof process&&process&&process.emitWarning?process.emitWarning(c):console.warn(c),e=!0}var s=[Object.assign(new a.Token("link_open","a",1),{attrs:[].concat(i.permalinkClass?[["class",i.permalinkClass]]:[],[["href",i.permalinkHref(r,a)]],Object.entries(i.permalinkAttrs(r,a)))}),Object.assign(new a.Token("html_block","",0),{content:i.permalinkSymbol,meta:t}),new a.Token("link_close","a",-1)];i.permalinkSpace&&a.tokens[l+1].children[n[i.permalinkBefore]](Object.assign(new a.Token("text","",0),{content:" "})),(o=a.tokens[l+1].children)[n[i.permalinkBefore]].apply(o,s)}function i(e){return"#"+e}function a(e){return{}}var l={class:"header-anchor",symbol:"#",renderHref:i,renderAttrs:a};function o(e){function n(t){return t=Object.assign({},n.defaults,t),function(n,r,i,a){return e(n,t,r,i,a)}}return n.defaults=Object.assign({},l),n.renderPermalinkImpl=e,n}var c=o(function(e,r,i,a,l){var o,c=[Object.assign(new a.Token("link_open","a",1),{attrs:[].concat(r.class?[["class",r.class]]:[],[["href",r.renderHref(e,a)]],r.ariaHidden?[["aria-hidden","true"]]:[],Object.entries(r.renderAttrs(e,a)))}),Object.assign(new a.Token("html_inline","",0),{content:r.symbol,meta:t}),new a.Token("link_close","a",-1)];if(r.space){var s="string"==typeof r.space?r.space:" ";a.tokens[l+1].children[n[r.placement]](Object.assign(new a.Token("string"==typeof r.space?"html_inline":"text","",0),{content:s}))}(o=a.tokens[l+1].children)[n[r.placement]].apply(o,c)});Object.assign(c.defaults,{space:!0,placement:"after",ariaHidden:!1});var s=o(c.renderPermalinkImpl);s.defaults=Object.assign({},c.defaults,{ariaHidden:!0});var f=o(function(e,n,t,r,i){var a=[Object.assign(new r.Token("link_open","a",1),{attrs:[].concat(n.class?[["class",n.class]]:[],[["href",n.renderHref(e,r)]],Object.entries(n.renderAttrs(e,r)))})].concat(n.safariReaderFix?[new r.Token("span_open","span",1)]:[],r.tokens[i+1].children,n.safariReaderFix?[new r.Token("span_close","span",-1)]:[],[new r.Token("link_close","a",-1)]);r.tokens[i+1]=Object.assign(new r.Token("inline","",0),{children:a})});Object.assign(f.defaults,{safariReaderFix:!1});var u=o(function(e,r,i,a,l){var o;if(!["visually-hidden","aria-label","aria-describedby","aria-labelledby"].includes(r.style))throw new Error("`permalink.linkAfterHeader` called with unknown style option `"+r.style+"`");if(!["aria-describedby","aria-labelledby"].includes(r.style)&&!r.assistiveText)throw new Error("`permalink.linkAfterHeader` called without the `assistiveText` option in `"+r.style+"` style");if("visually-hidden"===r.style&&!r.visuallyHiddenClass)throw new Error("`permalink.linkAfterHeader` called without the `visuallyHiddenClass` option in `visually-hidden` style");var c=a.tokens[l+1].children.filter(function(e){return"text"===e.type||"code_inline"===e.type}).reduce(function(e,n){return e+n.content},""),s=[],f=[];if(r.class&&f.push(["class",r.class]),f.push(["href",r.renderHref(e,a)]),f.push.apply(f,Object.entries(r.renderAttrs(e,a))),"visually-hidden"===r.style){if(s.push(Object.assign(new a.Token("span_open","span",1),{attrs:[["class",r.visuallyHiddenClass]]}),Object.assign(new a.Token("text","",0),{content:r.assistiveText(c)}),new a.Token("span_close","span",-1)),r.space){var u="string"==typeof r.space?r.space:" ";s[n[r.placement]](Object.assign(new a.Token("string"==typeof r.space?"html_inline":"text","",0),{content:u}))}s[n[r.placement]](Object.assign(new a.Token("span_open","span",1),{attrs:[["aria-hidden","true"]]}),Object.assign(new a.Token("html_inline","",0),{content:r.symbol,meta:t}),new a.Token("span_close","span",-1))}else s.push(Object.assign(new a.Token("html_inline","",0),{content:r.symbol,meta:t}));"aria-label"===r.style?f.push(["aria-label",r.assistiveText(c)]):["aria-describedby","aria-labelledby"].includes(r.style)&&f.push([r.style,e]);var d=[Object.assign(new a.Token("link_open","a",1),{attrs:f})].concat(s,[new a.Token("link_close","a",-1)]);(o=a.tokens).splice.apply(o,[l+3,0].concat(d)),r.wrapper&&(a.tokens.splice(l,0,Object.assign(new a.Token("html_block","",0),{content:r.wrapper[0]+"\n"})),a.tokens.splice(l+3+d.length+1,0,Object.assign(new a.Token("html_block","",0),{content:r.wrapper[1]+"\n"})))});function d(e,n,t,r){var i=e,a=r;if(t&&Object.prototype.hasOwnProperty.call(n,i))throw new Error("User defined `id` attribute `"+e+"` is not unique. Please fix it in your Markdown to continue.");for(;Object.prototype.hasOwnProperty.call(n,i);)i=e+"-"+a,a+=1;return n[i]=!0,i}function p(e,n){n=Object.assign({},p.defaults,n),e.core.ruler.push("anchor",function(e){for(var t,i={},a=e.tokens,l=Array.isArray(n.level)?(t=n.level,function(e){return t.includes(e)}):function(e){return function(n){return n>=e}}(n.level),o=0;o<a.length;o++){var c=a[o];if("heading_open"===c.type&&l(Number(c.tag.substr(1)))){var s=n.getTokensText(a[o+1].children),f=c.attrGet("id");f=null==f?d(n.slugify(s),i,!1,n.uniqueSlugStartIndex):d(f,i,!0,n.uniqueSlugStartIndex),c.attrSet("id",f),!1!==n.tabIndex&&c.attrSet("tabindex",""+n.tabIndex),"function"==typeof n.permalink?n.permalink(f,n,e,o):(n.permalink||n.renderPermalink&&n.renderPermalink!==r)&&n.renderPermalink(f,n,e,o),o=a.indexOf(c),n.callback&&n.callback(c,{slug:f,title:s})}}})}return Object.assign(u.defaults,{style:"visually-hidden",space:!0,placement:"after",wrapper:null}),p.permalink={__proto__:null,legacy:r,renderHref:i,renderAttrs:a,makePermalink:o,linkInsideHeader:c,ariaHidden:s,headerLink:f,linkAfterHeader:u},p.defaults={level:1,slugify:function(e){return encodeURIComponent(String(e).trim().toLowerCase().replace(/\s+/g,"-"))},uniqueSlugStartIndex:1,tabIndex:"-1",getTokensText:function(e){return e.filter(function(e){return["text","code_inline"].includes(e.type)}).map(function(e){return e.content}).join("")},permalink:!1,renderPermalink:r,permalinkClass:s.defaults.class,permalinkSpace:s.defaults.space,permalinkSymbol:"¶",permalinkBefore:"before"===s.defaults.placement,permalinkHref:s.defaults.renderHref,permalinkAttrs:s.defaults.renderAttrs},p.default=p,p});

function slugify (x) {
    return encodeURIComponent(String(x).trim().toLowerCase().replace(/\s+/g, '-'))
}

function htmlencode (x) {
    /*
      // safest, delegate task to native -- IMPORTANT: enabling this breaks both jest and runkit, but with browserify it's fine
      if (document && document.createElement) {
        const el = document.createElement("div")
        el.innerText = x
        return el.innerHTML
      }
    */

    return String(x)
        .replace(/&/g, '&amp;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
}

function tocPlugin (md, options) {
    options = Object.assign({}, {
        placeholder: '(\\$\\{toc\\}|\\[\\[?_?toc_?\\]?\\]|\\$\\<toc(\\{[^}]*\\})\\>)',
        slugify: slugify,
        uniqueSlugStartIndex: 1,
        containerClass: 'table-of-contents',
        containerId: undefined,
        listClass: undefined,
        itemClass: undefined,
        linkClass: undefined,
        level: 1,
        listType: 'ol',
        format: undefined,
        callback: undefined/* function(html, ast) {} */
    }, options)

    let ast
    const pattern = new RegExp('^' + options.placeholder + '$', 'i')

    function toc (state, startLine, endLine, silent) {
        let token
        const pos = state.bMarks[startLine] + state.tShift[startLine]
        const max = state.eMarks[startLine]

        // use whitespace as a line tokenizer and extract the first token
        // to test against the placeholder anchored pattern, rejecting if false
        const lineFirstToken = state.src.slice(pos, max).split(' ')[0]
        if (!pattern.test(lineFirstToken)) return false

        if (silent) return true

        const matches = pattern.exec(lineFirstToken)
        let inlineOptions = {}
        if (matches !== null && matches.length === 3) {
            try {
                inlineOptions = JSON.parse(matches[2])
            } catch (ex) {
                // silently ignore inline options
            }
        }

        state.line = startLine + 1

        token = state.push('tocOpen', 'nav', 1)
        token.markup = ''
        token.map = [startLine, state.line]
        token.inlineOptions = inlineOptions

        token = state.push('tocBody', '', 0)
        token.markup = ''
        token.map = [startLine, state.line]
        token.inlineOptions = inlineOptions
        token.children = []

        token = state.push('tocClose', 'nav', -1)
        token.markup = ''

        return true
    }

    md.renderer.rules.tocOpen = function (tokens, idx/* , options, env, renderer */) {
        let _options = Object.assign({}, options)
        if (tokens && idx >= 0) {
            const token = tokens[idx]
            _options = Object.assign(_options, token.inlineOptions)
        }
        const id = _options.containerId ? ` id="${htmlencode(_options.containerId)}"` : ''
        return `<nav${id} class="${htmlencode(_options.containerClass)}">`
    }

    md.renderer.rules.tocClose = function (/* tokens, idx, options, env, renderer */) {
        return '</nav>'
    }

    md.renderer.rules.tocBody = function (tokens, idx/* , options, env, renderer */) {
        let _options = Object.assign({}, options)
        if (tokens && idx >= 0) {
            const token = tokens[idx]
            _options = Object.assign(_options, token.inlineOptions)
        }

        const uniques = {}
        function unique (s) {
            let u = s
            let i = _options.uniqueSlugStartIndex
            while (Object.prototype.hasOwnProperty.call(uniques, u)) u = `${s}-${i++}`
            uniques[u] = true
            return u
        }

        const isLevelSelectedNumber = selection => level => level >= selection
        const isLevelSelectedArray = selection => level => selection.includes(level)

        const isLevelSelected = Array.isArray(_options.level)
            ? isLevelSelectedArray(_options.level)
            : isLevelSelectedNumber(_options.level)

        function ast2html (tree) {
            const listClass = _options.listClass ? ` class="${htmlencode(_options.listClass)}"` : ''
            const itemClass = _options.itemClass ? ` class="${htmlencode(_options.itemClass)}"` : ''
            const linkClass = _options.linkClass ? ` class="${htmlencode(_options.linkClass)}"` : ''

            if (tree.c.length === 0) return ''

            let buffer = ''
            if (tree.l === 0 || isLevelSelected(tree.l)) {
                buffer += (`<${htmlencode(_options.listType) + listClass}>`)
            }
            tree.c.forEach(node => {
                if (isLevelSelected(node.l)) {
                    buffer += (`<li${itemClass}><a${linkClass} href="#${unique(options.slugify(node.n))}">${typeof _options.format === 'function' ? _options.format(node.n, htmlencode) : htmlencode(node.n)}</a>${ast2html(node)}</li>`)
                } else {
                    buffer += ast2html(node)
                }
            })
            if (tree.l === 0 || isLevelSelected(tree.l)) {
                buffer += (`</${htmlencode(_options.listType)}>`)
            }
            return buffer
        }

        return ast2html(ast)
    }

    function headings2ast (tokens) {
        const ast = { l: 0, n: '', c: [] }
        const stack = [ast]

        for (let i = 0, iK = tokens.length; i < iK; i++) {
            const token = tokens[i]
            if (token.type === 'heading_open') {
                const key = (
                    tokens[i + 1]
                        .children
                        .filter(function (token) { return token.type === 'text' || token.type === 'code_inline' })
                        .reduce(function (s, t) { return s + t.content }, '')
                )

                const node = {
                    l: parseInt(token.tag.substr(1), 10),
                    n: key,
                    c: []
                }

                if (node.l > stack[0].l) {
                    stack[0].c.push(node)
                    stack.unshift(node)
                } else if (node.l === stack[0].l) {
                    stack[1].c.push(node)
                    stack[0] = node
                } else {
                    while (node.l <= stack[0].l) stack.shift()
                    stack[0].c.push(node)
                    stack.unshift(node)
                }
            }
        }

        return ast
    }

    md.core.ruler.push('generateTocAst', function (state) {
        const tokens = state.tokens
        ast = headings2ast(tokens)

        if (typeof options.callback === 'function') {
            options.callback(
                md.renderer.rules.tocOpen() + md.renderer.rules.tocBody() + md.renderer.rules.tocClose(),
                ast
            )
        }
    })

    md.block.ruler.before('heading', 'toc', toc, {
        alt: ['paragraph', 'reference', 'blockquote']
    })
}




let baseUri = (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') ? 'http://192.168.8.55:8089' : '';
     

function writeText(message) {
    const textarea = document.createElement("textarea");
    textarea.style.position = 'fixed';
    textarea.style.right = '100%';
    document.body.appendChild(textarea);
    textarea.value = message;
    textarea.select();
    document.execCommand('copy');
    textarea.remove();
}

function bindAllCodeElements() {
    const toast = document.getElementById('toast');

    document.querySelectorAll('.code-header')
        .forEach(x => {
            x.querySelector('button').addEventListener('click', ev => {
                writeText(x.nextSibling.textContent);
                toast.setAttribute('message', '已成功复制到剪切板。');
            });
        });

}

function lazyLoadImages() {
    const observer = new IntersectionObserver(function (entries) {
        Array.prototype.forEach.call(entries, function (entry) {
            if (entry.isIntersecting) {
                observer.unobserve(entry.target);
                entry.target.src = entry.target.getAttribute("data-src");
            }
        });
    });
    Array.prototype.forEach.call(document.querySelectorAll('img'), function (image) {
        if (image.hasAttribute('data-src'))
            observer.observe(image);
    });
}


document.addEventListener('DOMContentLoaded', (event) => {

});

async function loadData(id) {
    const res = await fetch(`${baseUri}/api/note?id=${id}`)
    return res.json();
}

async function render() {
    const obj = await loadData(new URL(window.location).searchParams.get('id'));
    document.title = `${obj.title} - 回形针`;
    const crumb = document.querySelector('.crumb');
    
    const articleTitle = document.querySelector('.article-title');
    articleTitle.textContent = obj.title;
    const articleInfos = document.querySelector('.article-infos span');
    const t = new Date(obj.update_at)
    articleInfos.textContent = `发布于${t.getFullYear()}-${(t.getMonth() + 1).toString().padStart(2, '0')}-${(t.getDate()).toString().padStart(2, '0')} ${(t.getHours()).toString().padStart(2, '0')}:${(t.getMinutes()).toString().padStart(2, '0')}:${(t.getSeconds()).toString().padStart(2, '0')}`;
    const content = document.querySelector('.content');
    const md = window.markdownit({
        linkify: true,
        highlight: function (str, lang) {
            if (lang && hljs.getLanguage(lang)) {
                try {
                    return hljs.highlight(str, { language: lang }).value;
                } catch (__) {}
            }

            return ''; // use external default escaping
        }
    })
        .use(markdownItAnchor)
        .use(tocPlugin);
    md.renderer.rules.table_open = function(tokens, idx, options, env, self) {
        return `<div class='table-wrapper'>` + self.renderToken(tokens, idx, options);
    };
    md.renderer.rules.table_close = function(tokens, idx, options, env, self) {
        return self.renderToken(tokens, idx, options) + `</div>`
    };
    content.innerHTML = md.render(obj.content);
    bindAllCodeElements();
    lazyLoadImages();
    // document.querySelectorAll('pre code').forEach((el) => {
    //     hljs.highlightElement(el);
    // });
}

render();
