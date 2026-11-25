const form = document.getElementById('form_cadastro');
const msgSucesso = document.getElementById('msg_sucesso');
const msgErro = document.getElementById('msg_erro');

// Se já estiver logado, redireciona para o perfil
if (estaAutenticado()) {
    window.location.href = '../pg_4/pg4.html';
}

form.addEventListener('submit', async (evento) => {
    evento.preventDefault();

    msgSucesso.style.display = 'none';
    msgErro.style.display = 'none';

    const nome = form.elements.name.value.trim();
    const email = form.elements.email.value.trim();
    const senha = form.elements.password.value;

    const btn = form.querySelector('button[type="submit"]');
    btn.disabled = true;
    btn.textContent = 'Criando...';

    try {
        const resposta = await fetch('http://localhost:8081/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                name: nome,
                email: email,
                password: senha
            })
        });

        if (!resposta.ok) {
            const erro = await resposta.json();
            mostrarErro(erro.message || 'Erro ao criar conta. Tente novamente.');
            return;
        }

        await resposta.json();
        form.reset();
        msgSucesso.style.display = 'flex';

        setTimeout(() => {
            window.location.href = '../pg_6/login.html';
        }, 1500);
    } catch (e) {
        console.error('Erro ao cadastrar:', e);
        mostrarErro('Não foi possível conectar ao servidor. Verifique se o backend está rodando.');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Criar Conta';
    }
});

function mostrarErro(mensagem) {
    msgErro.textContent = mensagem;
    msgErro.style.display = 'flex';
}
