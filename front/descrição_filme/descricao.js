const API_URL = 'http://localhost:8080/api';
// Pega o ID do filme que veio na URL (ex: .../descrição.html?id=tt0111161)
const parametros = new URLSearchParams(window.location.search);
const idFilme = parametros.get("id");

const banner = document.getElementById("banner");
const elNome = document.getElementById("nome_filme");
const elPoster = document.getElementById("poster_filme");
const elAno = document.getElementById("ano_filme");
const elNota = document.getElementById("nota_filme");
const elValorNota = document.querySelector(".valor-nota");
const elGeneros = document.getElementById("generos_filme");
const elSinopse = document.getElementById("sinopse_filme");
const elDiretor = document.getElementById("diretor_filme");
const elElenco = document.getElementById("elenco_filme");
const elElencoAvatares = document.getElementById("elenco_avatares");
const elReleased = document.getElementById("released_filme");
const elRuntime = document.getElementById("runtime_filme");
const elComentarioTexto = document.getElementById("comentario_texto");
const elBtnPublicar = document.getElementById("btn_publicar");
const elComentariosLista = document.getElementById("comentarios_lista");

const IMG_FALLBACK = "https://via.placeholder.com/300x450?text=Sem+imagem";
const MSG_INDISPONIVEL = "Não disponível";

// Valida se há ID na URL
if (!idFilme) {
    exibirErro("ID do filme não informado na URL.");
    throw new Error("ID do filme não informado na URL.");
}

// Busca os dados completos do filme no Cinemeta
fetch(`https://v3-cinemeta.strem.io/meta/movie/${idFilme}.json`)
    .then(resposta => {
        if (!resposta.ok) {
            throw new Error("Filme não encontrado na API.");
        }
        return resposta.json();
    })
    .then(dados => {
        const filme = dados.meta;
        if (!filme) {
            exibirErro("Dados do filme não encontrados.");
            return;
        }

        preencherPagina(filme);
    })
    .catch(erro => {
        console.error("Erro ao carregar filme:", erro);
        exibirErro(erro.message || "Não foi possível carregar o filme.");
    });

function preencherPagina(filme) {
    // Título da aba e da página
    document.title = filme.name || "Descrição do filme";
    if (elNome) elNome.textContent = filme.name || MSG_INDISPONIVEL;

    // Imagem de fundo do banner (com fallback)
    if (banner && filme.background) {
        banner.style.setProperty("--imagem-fundo", `url(${filme.background})`);
    } else if (banner) {
        banner.style.setProperty(
            "--imagem-fundo",
            "linear-gradient(180deg, #0c1224 0%, #141b2d 100%)"
        );
    }

    // Pôster
    if (elPoster) {
        elPoster.src = filme.poster || IMG_FALLBACK;
        elPoster.alt = `Cartaz do filme ${filme.name || ""}`;
        elPoster.onerror = () => {
            elPoster.src = IMG_FALLBACK;
        };
    }

    // Ano de lançamento (preferência: year > releaseInfo > extraído de released)
    const ano = filme.year
        || filme.releaseInfo
        || (filme.released ? String(filme.released).slice(0, 4) : null);
    if (elAno) elAno.textContent = ano ? String(ano) : MSG_INDISPONIVEL;

    // Nota IMDb com ícone de estrela
    if (elNota && elValorNota) {
        if (filme.imdbRating) {
            elValorNota.textContent = filme.imdbRating;
            elNota.style.display = "flex";
        } else {
            elNota.style.display = "none";
        }
    }

    // Sinopse
    if (elSinopse) {
        elSinopse.textContent = filme.description || MSG_INDISPONIVEL;
    }

    // Gêneros como etiquetas visuais (suporta ambos 'genres' e 'genre' da API)
    const generos = Array.isArray(filme.genres) ? filme.genres
        : Array.isArray(filme.genre) ? filme.genre
        : [];
    renderizarGeneros(elGeneros, generos);

    // Direção
    if (elDiretor) {
        const diretor = Array.isArray(filme.director) ? filme.director.join(", ")
            : filme.director || MSG_INDISPONIVEL;
        elDiretor.textContent = diretor;
    }

    // Elenco
    if (elElencoAvatares) {
        const elenco = Array.isArray(filme.cast) ? filme.cast
            : filme.cast ? [filme.cast] : [];
        renderizarElenco(elElencoAvatares, elenco);
    } else if (elElenco) {
        const elencoTexto = Array.isArray(filme.cast) ? filme.cast.join(", ")
            : filme.cast || MSG_INDISPONIVEL;
        elElenco.textContent = elencoTexto;
    }

    async function buscarComentarios() {
    try {
        console.log("ID do filme:", idFilme);
        const resposta = await fetch(
            `${API_URL}/reviews/movie/${encodeURIComponent(idFilme)}`
        );

        if (!resposta.ok) {
            throw new Error(`Erro HTTP: ${resposta.status}`);
        }

        const comentarios = await resposta.json();

        console.log("Comentários recebidos:", comentarios);

    } catch (erro) {
        console.error("Erro ao buscar comentários:", erro);
    }
}
    // Inicializa seção de comentários
    inicializarComentarios();
    buscarComentarios();

    // Data de lançamento formatada (fallback para ano)
    if (elReleased) {
        elReleased.textContent = formatarData(filme.released)
            || (ano ? String(ano) : MSG_INDISPONIVEL);
    }

    // Duração
    if (elRuntime) {
        elRuntime.textContent = filme.runtime ? `${filme.runtime}` : MSG_INDISPONIVEL;
    }
}

function renderizarGeneros(container, generos) {
    if (!container) return;
    container.innerHTML = "";

    if (!generos || generos.length === 0) {
        container.innerHTML = `<span style="color:var(--cor-texto-secundario); font-size:0.85rem;">${MSG_INDISPONIVEL}</span>`;
        return;
    }

    generos.forEach(genero => {
        const tag = document.createElement("span");
        tag.className = "tag-genero";
        tag.innerHTML = `<i class="fa-solid fa-tag"></i>${genero}`;
        container.appendChild(tag);
    });
}

function formatarData(iso) {
    if (!iso) return null;
    try {
        const data = new Date(iso);
        if (isNaN(data.getTime())) return null;
        return data.toLocaleDateString("pt-BR", {
            day: "numeric",
            month: "long",
            year: "numeric"
        });
    } catch (e) {
        return null;
    }
}

function exibirErro(mensagem) {
    document.title = "Erro - MOVIE WATCH";
    if (elNome) elNome.textContent = "Erro ao carregar o filme";
    if (elSinopse) elSinopse.textContent = mensagem;
    if (banner) {
        banner.style.setProperty(
            "--imagem-fundo",
            "linear-gradient(180deg, #0c1224 0%, #141b2d 100%)"
        );
    }
}

/* ---- Elenco: avatares circulares com iniciais (fallback) ---- */
function getIniciais(nome) {
    if (!nome) return "?";
    const partes = nome.trim().split(/\s+/);
    if (partes.length >= 2) {
        return (partes[0][0] + partes[partes.length - 1][0]).toUpperCase();
    }
    return partes[0][0].toUpperCase();
}

function renderizarElenco(container, elenco) {
    if (!container) return;
    container.innerHTML = "";

    if (!elenco || elenco.length === 0) {
        container.innerHTML = `<span style="color:var(--cor-texto-secundario); font-size:0.85rem;">${MSG_INDISPONIVEL}</span>`;
        return;
    }

    elenco.forEach(ator => {
        const wrapper = document.createElement("div");
        wrapper.className = "ator";

        const avatar = document.createElement("div");
        avatar.className = "avatar";
        avatar.setAttribute("aria-label", ator);
        avatar.textContent = getIniciais(ator);

        const nome = document.createElement("span");
        nome.className = "ator-nome";
        nome.textContent = ator;

        wrapper.appendChild(avatar);
        wrapper.appendChild(nome);
        container.appendChild(wrapper);
    });
}

/* ---- Comentários (sem backend, apenas UI) ---- */
function inicializarComentarios() {
    if (!elComentarioTexto || !elBtnPublicar || !elComentariosLista) return;

    // Campo permite digitação (já habilitado por padrão no HTML)
    elComentarioTexto.addEventListener("input", () => {
        elBtnPublicar.disabled = elComentarioTexto.value.trim() === "";
    });
    elBtnPublicar.disabled = elComentarioTexto.value.trim() === "";

    // Botão publicar: sem persistência nesta etapa
    elBtnPublicar.addEventListener("click", () => {
        const texto = elComentarioTexto.value.trim();
        if (!texto) return;

        // Estrutura pronta para futura integração com backend
        // Por enquanto, apenas limpa o campo e mantém estado vazio
        elComentarioTexto.value = "";
        elBtnPublicar.disabled = true;
        // Não adiciona comentário falso à lista
    });
}
