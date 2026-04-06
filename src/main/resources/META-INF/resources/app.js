const { createApp, ref, reactive, onMounted, watch, nextTick, computed } = Vue;

createApp({
  setup() {
    const moedaSelecionada = "USD";
    const carregando = ref(false);
    const erro = ref("");
    const cotacaoAtual = reactive({
      moeda: "",
      paridade: "",
      compra: 0,
      venda: 0,
      maxima: 0,
      minima: 0,
      variacaoPercentual: 0,
      dataHoraCotacao: ""
    });
    const historico = ref([]);
    const calculadora = reactive({
      valor: 100,
      origem: "BRL",
      destino: "USD",
      valorConvertido: null,
      cotacaoUtilizada: null,
      dataHoraCotacao: ""
    });
    const inverterCotacao = ref(false);

    // Exchange comparator
    const exchangeValorBRL = ref(5000);
    const exchangeCarregando = ref(false);
    const exchangeErro = ref("");
    const exchangeResultados = ref([]);

    let chart;

    const paridadeExibicao = computed(() => {
      if (inverterCotacao.value) {
        return `BRL-${cotacaoAtual.moeda}`;
      }
      return cotacaoAtual.paridade || `${cotacaoAtual.moeda}-BRL`;
    });

    const valoresExibicao = computed(() => {
      if (inverterCotacao.value) {
        return {
          compra: cotacaoAtual.venda > 0 ? (1 / cotacaoAtual.venda) : 0,
          venda: cotacaoAtual.compra > 0 ? (1 / cotacaoAtual.compra) : 0,
          maxima: cotacaoAtual.minima > 0 ? (1 / cotacaoAtual.minima) : 0,
          minima: cotacaoAtual.maxima > 0 ? (1 / cotacaoAtual.maxima) : 0,
          variacaoPercentual: -cotacaoAtual.variacaoPercentual
        };
      }
      return {
        compra: cotacaoAtual.compra,
        venda: cotacaoAtual.venda,
        maxima: cotacaoAtual.maxima,
        minima: cotacaoAtual.minima,
        variacaoPercentual: cotacaoAtual.variacaoPercentual
      };
    });

    const formatarNumero = (valor, casas = 4) =>
      Number(valor || 0).toLocaleString("pt-BR", { minimumFractionDigits: casas, maximumFractionDigits: casas });

    const formatarMoeda = (valor, casas = 2) =>
      Number(valor || 0).toLocaleString("pt-BR", { minimumFractionDigits: casas, maximumFractionDigits: casas });

    const toggleInverterCotacao = () => {
      inverterCotacao.value = !inverterCotacao.value;
      if (inverterCotacao.value) {
        const temp = calculadora.origem;
        calculadora.origem = calculadora.destino;
        calculadora.destino = temp;
      } else {
        calculadora.origem = "BRL";
        calculadora.destino = moedaSelecionada;
      }
      calcularConversao();
    };

    const carregarDados = async () => {
      carregando.value = true;
      erro.value = "";
      inverterCotacao.value = false;
      calculadora.origem = "BRL";
      calculadora.destino = moedaSelecionada;

      try {
        const [atualResp, histResp] = await Promise.all([
          fetch(`api/cotacao/atual?moeda=${moedaSelecionada}`),
          fetch(`api/cotacao/historico/30dias?moeda=${moedaSelecionada}`)
        ]);

        if (!atualResp.ok || !histResp.ok) throw new Error("Falha ao consultar a API.");

        Object.assign(cotacaoAtual, await atualResp.json());
        historico.value = (await histResp.json()).historico;
        await nextTick();
        renderizarGrafico();
        await calcularConversao();
      } catch (e) {
        erro.value = e.message || "Nao foi possivel carregar os dados.";
      } finally {
        carregando.value = false;
      }
    };

    const calcularConversao = async () => {
      erro.value = "";
      try {
        const resp = await fetch(`api/cotacao/calcular`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            moeda: moedaSelecionada,
            valor: parseFloat(calculadora.valor),
            moedaOrigem: calculadora.origem,
            moedaDestino: calculadora.destino
          })
        });
        if (!resp.ok) throw new Error(await resp.text() || "Erro ao calcular conversao.");
        const data = await resp.json();
        calculadora.valorConvertido = data.valorConvertido;
        calculadora.cotacaoUtilizada = data.cotacaoUtilizada;
        calculadora.dataHoraCotacao = data.dataHoraCotacao;
      } catch (e) {
        erro.value = e.message || "Erro ao calcular conversao.";
      }
    };

    const buscarCotacoesExchanges = async () => {
      exchangeCarregando.value = true;
      exchangeErro.value = "";
      exchangeResultados.value = [];

      try {
        const resp = await fetch(`api/exchange/cotacao`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            valorBRL: parseFloat(exchangeValorBRL.value)
          })
        });

        if (!resp.ok) {
          const body = await resp.text();
          throw new Error(body || "Erro ao consultar exchanges.");
        }

        exchangeResultados.value = await resp.json();
      } catch (e) {
        exchangeErro.value = e.message || "Erro ao buscar cotacoes das exchanges.";
      } finally {
        exchangeCarregando.value = false;
      }
    };

    const inverterMoedas = () => {
      const origem = calculadora.origem;
      calculadora.origem = calculadora.destino;
      calculadora.destino = origem;
      calcularConversao();
    };

    const renderizarGrafico = () => {
      const canvas = document.getElementById("historicoChart");
      if (!canvas) return;
      if (chart) chart.destroy();

      const dadosGrafico = historico.value.map(item => {
        if (inverterCotacao.value) {
          return {
            data: item.data,
            venda: item.compra > 0 ? (1 / item.compra) : 0
          };
        }
        return item;
      });

      chart = new Chart(canvas, {
        type: "line",
        data: {
          labels: dadosGrafico.map(item => item.data),
          datasets: [{
            label: `Venda ${paridadeExibicao.value}`,
            data: dadosGrafico.map(item => Number(item.venda)),
            borderColor: "#ff6b35",
            backgroundColor: "rgba(255,107,53,0.15)",
            fill: true,
            tension: 0.28,
            borderWidth: 3,
            pointRadius: 2
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { labels: { color: "#f8f2e7", font: { family: "Space Grotesk" } } }
          },
          scales: {
            x: { ticks: { color: "#d8d3cb" }, grid: { color: "rgba(255,255,255,0.08)" } },
            y: { ticks: { color: "#d8d3cb" }, grid: { color: "rgba(255,255,255,0.08)" } }
          }
        }
      });
    };

    const getExchangeIcon = (exchange) => {
      const icons = {
        'BINANCE': '🟡',
        'BITGET': '🔵',
        'KUCOIN': '🟢'
      };
      return icons[exchange.toUpperCase()] || '📊';
    };

    const abrirExchange = (url) => {
      if (url) {
        window.open(url, '_blank');
      }
    };

    const simulacaoArbitragem = computed(() => {
      if (!exchangeResultados.value || exchangeResultados.value.length === 0) return null;
      let menorCompra = null;
      let maiorVenda = null;
      
      exchangeResultados.value.forEach(ex => {
        if (!menorCompra || ex.compra < menorCompra.compra) menorCompra = ex;
        if (!maiorVenda || ex.venda > maiorVenda.venda) maiorVenda = ex;
      });

      if (!menorCompra || !maiorVenda || !exchangeValorBRL.value) return null;

      const qtdUsdt = exchangeValorBRL.value / menorCompra.compra;
      const valorVendaBRL = qtdUsdt * maiorVenda.venda;
      const diferencaBRL = valorVendaBRL - exchangeValorBRL.value;
      const diferencaPct = (diferencaBRL / exchangeValorBRL.value) * 100;

      const taxaCompraPct = parseFloat(menorCompra.taxaTaker.replace('%', ''));
      const taxaVendaPct = parseFloat(maiorVenda.taxaTaker.replace('%', ''));
      const valorTaxaCompra = qtdUsdt * menorCompra.compra * (taxaCompraPct / 100);
      const valorTaxaVenda = qtdUsdt * maiorVenda.venda * (taxaVendaPct / 100);
      const diferencaLiquidaBRL = diferencaBRL - valorTaxaCompra - valorTaxaVenda;
      const diferencaLiquidaPct = (diferencaLiquidaBRL / exchangeValorBRL.value) * 100;

      const diferencaUSD = diferencaBRL / menorCompra.compra;
      const diferencaLiquidaUSD = diferencaLiquidaBRL / menorCompra.compra;

      return {
        menorCompra,
        maiorVenda,
        diferencaBRL,
        diferencaPct,
        diferencaLiquidaBRL,
        diferencaLiquidaPct,
        diferencaUSD,
        diferencaLiquidaUSD
      };
    });

    watch(inverterCotacao, () => { renderizarGrafico(); calcularConversao(); });
    onMounted(() => {
      carregarDados();
      // Carregar cotacoes das exchanges automaticamente
      buscarCotacoesExchanges();
    });

    return {
      moedaSelecionada, carregando, erro, cotacaoAtual, historico, calculadora,
      inverterCotacao, paridadeExibicao, valoresExibicao, formatarNumero, formatarMoeda,
      calcularConversao, inverterMoedas, toggleInverterCotacao,
      exchangeValorBRL, exchangeCarregando, exchangeErro, exchangeResultados,
      buscarCotacoesExchanges, getExchangeIcon, simulacaoArbitragem
    };
  },
  template: `
    <main class="page-shell">
      <button class="btn-inverter-float" @click="toggleInverterCotacao" :title="inverterCotacao ? 'Mostrar USD-BRL' : 'Mostrar BRL-USD'">
        <span class="icon-inverter">⇄</span>
        <span class="label-inverter">{{ inverterCotacao ? 'BRL-USD' : 'USD-BRL' }}</span>
      </button>

      <section class="hero">
        <div>
          <p class="eyebrow">Dicop Cambio Dashboard</p>
          <h1>Monitor de cambio com cotacao ao vivo e historico dos ultimos 30 dias.</h1>
          <p class="hero-copy">Acompanhe a variacao do Dolar (USD) e simule conversoes instantaneas.</p>
        </div>
      </section>

      <p v-if="erro" class="error">{{ erro }}</p>
      <p v-if="carregando" class="loading">Carregando dados...</p>

      <section class="cards">
        <article class="card accent">
          <span class="card-label">Compra</span>
          <strong>{{ formatarNumero(valoresExibicao.compra) }}</strong>
          <small>{{ paridadeExibicao }}</small>
        </article>
        <article class="card">
          <span class="card-label">Venda</span>
          <strong>{{ formatarNumero(valoresExibicao.venda) }}</strong>
          <small>Atualizada em {{ cotacaoAtual.dataHoraCotacao }}</small>
        </article>
        <article class="card">
          <span class="card-label">Maxima</span>
          <strong>{{ formatarNumero(valoresExibicao.maxima) }}</strong>
          <small>Minima {{ formatarNumero(valoresExibicao.minima) }}</small>
        </article>
        <article class="card">
          <span class="card-label">Variacao</span>
          <strong :class="valoresExibicao.variacaoPercentual >= 0 ? 'positivo' : 'negativo'">{{ formatarNumero(valoresExibicao.variacaoPercentual, 2) }}%</strong>
          <small>USD frente ao BRL</small>
        </article>
      </section>

      <!-- Card Comparador de Exchanges -->
      <section class="exchange-section">
        <article class="panel exchange-panel">
          <div class="panel-header">
            <div>
              <p class="eyebrow">Comparador</p>
              <h2>Preco USDT nas Exchanges</h2>
            </div>
          </div>

          <div class="form-grid">
            <label>Valor em BRL<input type="number" min="1" step="1" v-model="exchangeValorBRL" placeholder="Ex: 100"></label>
          </div>
          <div class="calculator-actions">
            <button class="primary" @click="buscarCotacoesExchanges" :disabled="exchangeCarregando">
              {{ exchangeCarregando ? 'Pesquisando...' : 'Pesquisar' }}
            </button>
          </div>

          <p v-if="exchangeErro" class="error">{{ exchangeErro }}</p>
          <p v-if="exchangeCarregando" class="loading">Consultando exchanges...</p>

          <section class="cards" style="margin-bottom:20px" v-if="simulacaoArbitragem">
            <article class="card">
              <span class="card-label">Comprar na {{ simulacaoArbitragem.menorCompra.exchange }}</span>
              <strong>{{ inverterCotacao ? formatarMoeda(1 / simulacaoArbitragem.menorCompra.compra, 4) : formatarMoeda(simulacaoArbitragem.menorCompra.compra, 3) }}</strong>
            </article>
            <article class="card">
              <span class="card-label">Vender na {{ simulacaoArbitragem.maiorVenda.exchange }}</span>
              <strong>{{ inverterCotacao ? formatarMoeda(1 / simulacaoArbitragem.maiorVenda.venda, 4) : formatarMoeda(simulacaoArbitragem.maiorVenda.venda, 3) }}</strong>
            </article>
            <article class="card">
              <span class="card-label">Simulacao</span>
              <div class="arb-grid">
                <div class="arb-col" :class="simulacaoArbitragem.diferencaBRL >= 0 ? 'positivo' : 'negativo'">
                  <div class="arb-label">Bruto</div>
                  <div class="arb-valor">{{ inverterCotacao ? 'USD ' + formatarMoeda(simulacaoArbitragem.diferencaUSD, 2) : 'R$ ' + formatarMoeda(simulacaoArbitragem.diferencaBRL, 2) }} ({{ simulacaoArbitragem.diferencaPct >= 0 ? '+' : '' }}{{ formatarNumero(simulacaoArbitragem.diferencaPct, 2) }}%)</div>
                </div>
                <div class="arb-col" :class="simulacaoArbitragem.diferencaLiquidaBRL >= 0 ? 'positivo' : 'negativo'">
                  <div class="arb-label">Liquido</div>
                  <div class="arb-valor">{{ inverterCotacao ? 'USD ' + formatarMoeda(simulacaoArbitragem.diferencaLiquidaUSD, 2) : 'R$ ' + formatarMoeda(simulacaoArbitragem.diferencaLiquidaBRL, 2) }} ({{ simulacaoArbitragem.diferencaLiquidaPct >= 0 ? '+' : '' }}{{ formatarNumero(simulacaoArbitragem.diferencaLiquidaPct, 2) }}%)</div>
                </div>
              </div>
            </article>
          </section>

          <div v-if="exchangeResultados.length > 0" class="exchange-table-container">
            <table class="exchange-table">
              <thead>
                <tr>
                  <th>Exchange</th>
                  <th>Compra ({{ inverterCotacao ? 'USD' : 'BRL' }})</th>
                  <th>Venda ({{ inverterCotacao ? 'USD' : 'BRL' }})</th>
                  <th>Taxa</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in exchangeResultados" :key="item.exchange" @click="abrirExchange(item.linkNegociacao)" style="cursor: pointer;">
                  <td class="exchange-name">
                    <span class="exchange-icon">{{ getExchangeIcon(item.exchange) }}</span>
                    {{ item.exchange }}
                  </td>
                  <td class="exchange-compra">
                    {{ inverterCotacao ? formatarMoeda(1 / item.compra, 4) : formatarMoeda(item.compra, 3) }}
                    <small v-if="inverterCotacao" style="display:block; font-size:0.8em; color:#888;">{{ formatarMoeda(item.compra, 3) }} BRL</small>
                    <small v-else style="display:block; font-size:0.8em; color:#888;">{{ formatarMoeda(1 / item.compra, 4) }} USD</small>
                  </td>
                  <td class="exchange-venda">
                    {{ inverterCotacao ? formatarMoeda(1 / item.venda, 4) : formatarMoeda(item.venda, 3) }}
                    <small v-if="inverterCotacao" style="display:block; font-size:0.8em; color:#888;">{{ formatarMoeda(item.venda, 3) }} BRL</small>
                    <small v-else style="display:block; font-size:0.8em; color:#888;">{{ formatarMoeda(1 / item.venda, 4) }} USD</small>
                  </td>
                  <td class="exchange-taxa">{{ item.taxaTaker }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </article>
      </section>

      <section class="content-grid">
        <article class="panel chart-panel">
          <div class="panel-header"><div><p class="eyebrow">Historico</p><h2>Ultimos 30 dias</h2></div></div>
          <div class="chart-wrap"><canvas id="historicoChart"></canvas></div>
        </article>

        <article class="panel">
          <div class="panel-header"><div><p class="eyebrow">Calculadora</p><h2>Conversao instantanea</h2></div></div>
          <div class="form-grid">
            <label>Valor<input type="number" min="0.01" step="0.01" v-model="calculadora.valor"></label>
            <label>Origem<select v-model="calculadora.origem"><option value="BRL">BRL</option><option value="USD">USD</option></select></label>
            <label>Destino<select v-model="calculadora.destino"><option value="BRL">BRL</option><option value="USD">USD</option></select></label>
          </div>
          <div class="calculator-actions">
            <button class="primary" @click="calcularConversao">Calcular</button>
            <button class="secondary" @click="inverterMoedas">Inverter</button>
          </div>
          <div class="result-box" v-if="calculadora.valorConvertido !== null">
            <span>Valor convertido</span>
            <strong>{{ formatarNumero(calculadora.valorConvertido) }}</strong>
            <small>Cotacao usada: {{ formatarNumero(calculadora.cotacaoUtilizada) }}</small>
            <small>Referencia: {{ calculadora.dataHoraCotacao }}</small>
          </div>
        </article>
      </section>
    </main>
  `
}).mount("#app");
