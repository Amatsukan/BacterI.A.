import { GraphicEntityModule } from './entity-module/GraphicEntityModule.js';

// -----------------------------------------------------------------------
// Módulos do viewer
// -----------------------------------------------------------------------
export const modules = [
    GraphicEntityModule
];

// -----------------------------------------------------------------------
// Identidade do jogo
// -----------------------------------------------------------------------
export const gameName = 'BacterI.A.';

// -----------------------------------------------------------------------
// Cores dos jogadores (Player 1 = azul, Player 2 = vermelho)
// -----------------------------------------------------------------------
export const playerColors = [
    '#22a1e4',
    '#ff1d5c',
];

// -----------------------------------------------------------------------
// Opções do viewer (botões/toggles visíveis no replay)
// -----------------------------------------------------------------------
export const options = [];

// -----------------------------------------------------------------------
// Dados de demonstração (undefined = sem replay de demo no IDE)
// -----------------------------------------------------------------------
export const demo = undefined

// -----------------------------------------------------------------------
// Configurações de renderização
// -----------------------------------------------------------------------
export const stepByStepAnimateSpeed = 500;   // ms por passo no modo step-by-step
export const overlayAlpha           = 0.2;   // transparência do overlay
export const defaultOverSampling    = 2;     // pixel ratio padrão (retina)
