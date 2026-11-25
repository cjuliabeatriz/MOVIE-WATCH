const API_URL = 'http://localhost:8080/api';
let filmes = [];
const areaFilmes = document.getElementById("filmes-todos");
const semResultados = document.getElementById("sem-resultados");

function renderizar(filmesParaMostrar) {
    areaFilmes.innerHTML = "";

    if (!filmesParaMostrar || filmesParaMostrar.length === 0) {
        semResultados.style.display = "block";
        return;
    }

    semResultados.style.display = "none";

    filmesParaMostrar.forEach(filme => {
        areaFilmes.innerHTML += `
            <div>
                <a href="../descrição_filme/descrição.html?id=${filme.imdb_id}">
                    <h2>${filme.name}</h2>
                    <img src="${filme.poster}" alt="${filme.name}">
                </a>
            </div>
        `;
    });
}

// Carrega TODOS os filmes da API de uma vez s� (sem paginacao)
fetch("https://cinemeta-catalogs.strem.io/top/catalog/movie/top.json")
    .then(resposta => resposta.json())
    .then(dados => {
        filmes = dados.metas;
        renderizar(filmes);
        console.log(filmes);
    })
    .catch(erro => console.error("Erro ao carregar filmes:", erro));

// Campo de pesquisa
const formulario = document.querySelector(".campo-busca");

formulario.addEventListener("submit", function (evento) {
    evento.preventDefault();

    const pesquisa = formulario.querySelector("input").value.trim();

    if (pesquisa === "") {
        renderizar(filmes);
        return;
    }

    fetch(`https://v3-cinemeta.strem.io/catalog/movie/top/search=${encodeURIComponent(pesquisa)}.json`)
        .then(resposta => resposta.json())
        .then(dados => {
            renderizar(dados.metas);
        })
        .catch(erro => console.error("Erro na pesquisa:", erro));
});
