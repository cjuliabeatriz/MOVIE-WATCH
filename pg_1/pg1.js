
 //cria um catalogo onde vai guaradr os filmes em uma lista, por enquanto vazia.
 //vamos guaradr nessa lista os filmes recebidos pela api.
let filmes = []; 

 //faz uma requisição á api do cinemata para buscar o catalogo de filmes.
fetch("https://cinemeta-catalogs.strem.io/top/catalog/movie/top.json")

 //quando a resposta x chegar, vai converter => para json
 //que é o que o js vai entender
 // . then vai guaradar a resposta no servidor
 //.then define o que acontece depois que a etapa anterior foi concluída.
.then(resposta => resposta.json()) 

 //quando o json estiver convertido, vai receber os dados retornados pela api.
.then(dados => {

    //acessa a prioridade "metas" dos dados recebidos,
    //que contém uma lista de filmes, e guarda essa lista na variável "filmes".
    filmes = dados.metas;

    //vai acrescentar os filmes na div criada no html.
    // ALTERADO AQUI: o nome da variável agora é areaFilmes.
    const areaFilmes = document.getElementById("filme");

    //filmes é a lista completa recebida pela api.
    filmes.forEach(filme => {
       //se introduziu no html, por estar dentro de um
       //forEach isso acontece para cada filme da lista. 
        areaFilmes.innerHTML += `
            <div>
                <a href="../descrição_filme/descrição.html?id=${filme.imdb_id}"> 
                <h2>${filme.name}</h2>
                <img src="${filme.poster}" alt="${filme.name}">

                </a>    
            </div>
        `;
    });  
    //${filme.imdb_id} --> identificao filme atual 
    //<h2>${filme.name}</h2> --> puxa o nome 
    //src="${filme.poster}" alt="${filme.name}" --> puxa que pelo poster vai ter a mesma funcionalidade
    //de entrar na descrição do filme só pela img. 

    console.log(filmes);

});
 //FAZENDO O CAMPO DE PESQUISA FUNCIONAR
   // a const formulário vai ter acesso a parte de busca 
 
 // Encontra o formulário de pesquisa no HTML pela classe "campo-busca".
const formulario = document.querySelector(".campo-busca");

// Fica esperando o usuário enviar o formulário, apertando Enter ou clicando no botão.
formulario.addEventListener("submit", function(evento) {

    // Impede que o navegador recarregue a página ao enviar o formulário.
    evento.preventDefault();

    // Encontra a caixa de texto dentro do formulário, pega o que foi digitado
    // e remove espaços em branco que estejam no começo ou no final.
    const pesquisa = formulario.querySelector("input").value.trim();

    // Verifica se o usuário deixou a caixa vazia.
    if (pesquisa === "") {

        // Encerra a função se não houver nenhum texto para pesquisar.
        return;
    }

    // Faz uma requisição à API Cinemeta usando o nome digitado na pesquisa.
    // encodeURIComponent prepara o texto para ser usado dentro da URL,
    // inclusive quando o nome tem espaços ou caracteres especiais.
    fetch(`https://v3-cinemeta.strem.io/catalog/movie/top/search=${encodeURIComponent(pesquisa)}.json`)

        // Converte a resposta recebida da API para um objeto JavaScript.
        .then(resposta => resposta.json())

        // Executa este bloco quando os dados já estiverem convertidos.
        .then(dados => {
            filmes.slice(0, 4)
            // Encontra a área do HTML onde os filmes serão exibidos.
            const areaFilmes = document.getElementById("filme");

            // Apaga os filmes que estavam aparecendo antes da nova pesquisa.
            areaFilmes.innerHTML = "";

            // Guarda na variável "filmes" a lista de resultados da pesquisa.
            areaFilmesilmes = dados.metas;

            // Percorre a lista, executando o código abaixo para cada filme.
            filmes.forEach(filme => {

                // Adiciona à página um título, um cartaz e um link para cada filme.
                // As crases permitem escrever HTML em várias linhas e inserir variáveis.
                areaFilmes.innerHTML += `
                    <div>
                        <a href="../descrição_filme/descrição.html?id=${filme.imdb_id}">
                            <h2>${filme.name}</h2>
                            <img src="${filme.poster}" alt="${filme.name}">
                        </a>
                    </div>
                `;
            });
        });
});
  