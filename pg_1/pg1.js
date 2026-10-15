//cria um catalogo onde vai guaradr os filmes em uma lista, por enquanto vazia.
//vamos guaradr nessa lista os filmes recebidos pela api.
let filmes = [];

let paginaAtual = 0;

// ALTERADO: criei esta constante para não repetir o número 5 no código.
// Se quiser mostrar mais ou menos filmes por página, mude só aqui.
const FILMES_POR_PAGINA = 5;

// ALTERADO: antes o document.getElementById("filme") ficava dentro da função.
// Agora pego os elementos uma vez só, aqui em cima.
const areaFilmes = document.getElementById("filme");
const bntProximo = document.getElementById("bntProximo");
// ALTERADO: faltava pegar o botão da seta esquerda.
const bntAnterior = document.getElementById("bntAnterior");

function MostrarFilmes() {
    const inicio = paginaAtual * FILMES_POR_PAGINA;
    // ALTERADO: renomeei "caminho" para "fim", que é o nome que faz sentido
    // (o filme onde a página termina).
    const fim = inicio + FILMES_POR_PAGINA;

    // ALTERADO: calcula quantas páginas existem no total (usado para desativar a seta).
    const totalPaginas = Math.ceil(filmes.length / FILMES_POR_PAGINA);

    areaFilmes.innerHTML = "";

    filmes.slice(inicio, fim).forEach(filme => {
        areaFilmes.innerHTML += `
            <div>
                <a href="../descrição_filme/descrição.html?id=${filme.imdb_id}"> 
                <h2>${filme.name}</h2>
                <img src="${filme.poster}" alt="${filme.name}">
                </a>    
            </div>
        `;
        // ALTERADO: removi a linha solta "paginaAtual;" que estava aqui.
        // Ela não fazia nada.
    });

    // ALTERADO: desativa a seta esquerda na primeira página
    // e a seta direita na última página.
    bntAnterior.disabled = paginaAtual === 0;
    bntProximo.disabled = paginaAtual >= totalPaginas - 1;
}

// ALTERADO: antes era bntProximo.addEventListener("caminho"), o que dava erro.
// O evento correto é "click", e é preciso passar uma função que diz o que fazer.
bntProximo.addEventListener("click", function () {
    // só avança se ainda houver filmes na próxima página
    if ((paginaAtual + 1) * FILMES_POR_PAGINA < filmes.length) {
        paginaAtual++;
        MostrarFilmes();
    }
});

// ALTERADO: este botão não tinha código nenhum. Agora a seta esquerda funciona.
bntAnterior.addEventListener("click", function () {
    if (paginaAtual > 0) {
        paginaAtual--;
        MostrarFilmes();
    }
});

//faz uma requisição á api do cinemata para buscar o catalogo de filmes.
fetch("https://cinemeta-catalogs.strem.io/top/catalog/movie/top.json")

    //quando a resposta x chegar, vai converter => para json
    //que é o que o js vai entender
    .then(resposta => resposta.json())

    //quando o json estiver convertido, vai receber os dados retornados pela api.
    .then(dados => {

        //acessa a prioridade "metas" dos dados recebidos,
        //que contém uma lista de filmes, e guarda essa lista na variável "filmes".
        filmes = dados.metas;

        // ALTERADO (A MAIS IMPORTANTE): faltava chamar MostrarFilmes() aqui.
        // Sem isso os filmes chegavam da API, mas nunca eram desenhados na tela.
        paginaAtual = 0;
        MostrarFilmes();

        console.log(filmes);
    })
    // ALTERADO: se a internet cair ou a API falhar, o erro aparece no console (F12).
    .catch(erro => console.error("Erro ao carregar filmes:", erro));

//FAZENDO O CAMPO DE PESQUISA FUNCIONAR

// Encontra o formulário de pesquisa no HTML pela classe "campo-busca".
const formulario = document.querySelector(".campo-busca");

// Fica esperando o usuário enviar o formulário, apertando Enter ou clicando no botão.
formulario.addEventListener("submit", function (evento) {

    // Impede que o navegador recarregue a página ao enviar o formulário.
    evento.preventDefault();

    // Pega o que foi digitado e remove espaços no começo e no fim.
    const pesquisa = formulario.querySelector("input").value.trim();

    // Se a caixa estiver vazia, encerra a função.
    if (pesquisa === "") {
        return;
    }

    // Faz uma requisição à API Cinemeta usando o nome digitado na pesquisa.
    // encodeURIComponent prepara o texto para ser usado dentro da URL.
    fetch(`https://v3-cinemeta.strem.io/catalog/movie/top/search=${encodeURIComponent(pesquisa)}.json`)

        .then(resposta => resposta.json())

        .then(dados => {
            // ALTERADO: removi "filmes.slice(0, 4)" e todo o código que desenhava
            // os filmes aqui dentro. O slice sozinho não fazia nada, e o resto
            // tinha o typo "areaFilmesilmes".

            // ALTERADO: agora os resultados da pesquisa substituem a lista "filmes",
            // voltamos para a página 0 e usamos a MESMA função MostrarFilmes().
            // Assim a pesquisa também funciona com as setas.
            filmes = dados.metas;
            paginaAtual = 0;
            MostrarFilmes();
        })
        // ALTERADO: mostra erro no console se a pesquisa falhar.
        .catch(erro => console.error("Erro na pesquisa:", erro));
});