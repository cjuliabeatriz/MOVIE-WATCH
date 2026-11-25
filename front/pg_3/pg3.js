const listaFavoritos = document.getElementById('lista_favoritos');
const vazio = document.getElementById('favoritos_vazio');

async function carregarFavoritos() {
    try {
        const resposta = await apiFetch('/favorites');

        if (!resposta.ok) {
            mostrarErro('Erro ao carregar favoritos.');
            return;
        }

        const favoritos = await resposta.json();

        if (!favoritos || favoritos.length === 0) {
            vazio.style.display = 'block';
            listaFavoritos.innerHTML = '';
            return;
        }

        vazio.style.display = 'none';
        listaFavoritos.innerHTML = '';

        for (const fav of favoritos) {
            await renderizarFavorito(fav);
        }
    } catch (erro) {
        console.error('Erro ao carregar favoritos:', erro);
        mostrarErro('Não foi possível carregar seus favoritos. Faça login novamente.');
    }
}

async function renderizarFavorito(fav) {
    const movieId = fav.movieId;

    let poster = 'https://via.placeholder.com/200x300?text=Sem+Imagem';
    let nome = 'Filme #' + movieId;

    try {
        const resp = await fetch(`https://v3-cinemeta.strem.io/catalog/movie/top/${movieId}.json`);
        if (resp.ok) {
            const dados = await resp.json();
            const filme = dados.metas && dados.metas[0];
            if (filme) {
                poster = filme.poster || poster;
                nome = filme.name || nome;
            }
        }
    } catch (e) {
        console.warn('Não foi possível buscar detalhes do filme ' + movieId, e);
    }

    const card = document.createElement('div');
    card.className = 'filme-card';
    card.innerHTML = `
        <img src="${poster}" alt="${nome}">
        <div class="filme-info">
            <h3>${nome}</h3>
            <button class="btn-remover" data-movie-id="${movieId}">
                <i class="fa-solid fa-trash"></i> Remover
            </button>
        </div>
    `;

    const btnRemover = card.querySelector('.btn-remover');
    btnRemover.addEventListener('click', () => removerFavorito(movieId, card));

    listaFavoritos.appendChild(card);
}

async function removerFavorito(movieId, card) {
    try {
        const resposta = await apiFetch(`/favorites/${movieId}`, {
            method: 'DELETE'
        });

        if (resposta.ok) {
            card.remove();
            const restante = listaFavoritos.querySelectorAll('.filme-card').length;
            if (restante === 0) {
                vazio.style.display = 'block';
            }
        } else {
            const erro = await resposta.json();
            alert(erro.message || 'Erro ao remover favorito.');
        }
    } catch (erro) {
        console.error('Erro ao remover favorito:', erro);
        alert('Não foi possível remover o favorito.');
    }
}

function mostrarErro(mensagem) {
    listaFavoritos.innerHTML = '';
    vazio.style.display = 'none';
    listaFavoritos.innerHTML = `<div class="erro">${mensagem}</div>`;
}

// Verifica autenticação antes de carregar
if (!estaAutenticado()) {
    window.location.href = '../pg_6/login.html';
} else {
    carregarFavoritos();
}
