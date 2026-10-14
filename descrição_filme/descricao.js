//pega o id do filme que veio na url
//na url u id do filme fica depois do ? pois dessa forma o site
//consegue buscar o filme pelo id
const parametros = new URLSearchParams(window.location.search); 
const idFilme = parametros.get("id"); 

console.log("ID do filme:", idFilme); //mostra o identificador extraído da URL
console.log("URL atual:", window.location.href); //mostra a rl completa na página 

//busca os dados completos do fime no cinemeta

//consulta o id na api (pelo fetch) pela url
fetch(`https://v3-cinemeta.strem.io/meta/movie/${idFilme}.json`)
.then(resposta => resposta.json())
.then(dados => {
    //DEPOIS DISSO Recebe os dados já interpretados e executa as instruções dentro das chaves { }
   
   // iformações do filme em expecifico
    const filme = dados.meta; 

    //colocaas informações nos espaços html 
    document.title = filme.name;
        document.getElementById("nome_filme").textContent = filme.name;
        document.getElementById("poster_filme").src = filme.poster;
        document.getElementById("sinopse_filme").textContent = filme.description || "Sinopse indisponível";
        document.getElementById("nota_filme").textContent = "Nota IMDb: " + (filme.imdbRating || "Não disponível");
        document.getElementById("ano_filme").textContent = "Ano: " + (filme.year || filme.releaseInfo || "Não disponível");
        document.getElementById("diretor_filme").textContent = "Direção: " + (filme.director?.join(", ") || "Não disponível");
        document.getElementById("elenco_filme").textContent = "Elenco: " + (filme.cast?.join(", ") || "Não disponível");
        document.getElementById("generos_filme").textContent = "Gêneros: " + (filme.genres?.join(", ") || "Não disponíveis");
})