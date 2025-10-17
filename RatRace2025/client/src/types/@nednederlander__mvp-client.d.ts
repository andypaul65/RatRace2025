declare module '@nednederlander/mvp-client' {
  export interface TabConfig {
    namespace: string;
    title: string;
    component: React.ComponentType<any>;
    hooks?: {
      onTabMount?: (namespace: string) => void;
      onTabUnmount?: (namespace: string) => void;
    };
    style?: React.CSSProperties;
  }

  export interface TabbedInterfaceProps {
    tabs: TabConfig[];
  }

  export class TabbedInterface extends React.Component<TabbedInterfaceProps> {}

  export interface SystemStateHook {
    sendMessage: (message: { type: string; content: string }) => Promise<{ type: string; content: string; namespace: string }>;
    loading: boolean;
    error: string | null;
  }

  export function useSystemState(namespace: string): SystemStateHook;
}